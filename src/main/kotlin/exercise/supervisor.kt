package exercise

import rx.Observable
import rx.subjects.PublishSubject
import java.time.Instant

/**
 * @version $Id$
 */
class Supervisor(vararg val rules: Rule) {

    /**
     * Isolated mutable state structure
     */
    private data class State(
            val environment: Environment,
            val ongoingSituations: Map<Rule, Situation>,
            val pastAlerts: Set<Rule>
    )

    // Our current state
    private var state = State(
            environment = Environment(Instant.EPOCH, mapOf()),
            ongoingSituations = mapOf(),
            pastAlerts = setOf()
    )

    // RX subject that emits alerts
    private val subject: PublishSubject<Alert> = PublishSubject.create()

    // ---------------------------------------------------------------------------------------------
    // Public API

    val alerts: Observable<Alert> = subject

    /**
     * Feed a new measurement (sensor value) to the supervisor.
     */
    fun push(measurement: SensorValue) {

        synchronized(state, {
            if (measurement.timestamp.isBefore(state.environment.time)) {
                // Prevent to use with randomly timed sensor values
                subject.onError(IllegalStateException("Events are expected to arrive in natural order"))
            }

            // Update state with new environment last received sensor value
            this.state = this.state.copy(environment = Environment(
                    time = measurement.timestamp,
                    conditions = this.state.environment.conditions.plus(
                            Pair(measurement.dimension, measurement.value))
            ))

            val rulesMatching = rules.filter { rule -> rule.evaluate(this.state.environment) }
            val newMatches = rulesMatching.filter { !this.state.ongoingSituations.containsKey(it) }
            val finishedMatches = this.state.ongoingSituations.keys.filter { !rulesMatching.contains(it) }

            finishedMatches.forEach { rule ->
                val situation = this.state.ongoingSituations[rule]!!
                if (this.state.environment.time.minusSeconds(rule.duration * 60).isAfter(situation.startedAt)) {
                    subject.onNext(Alert(
                            rule = rule,
                            startedAt = situation.startedAt,
                            finishedAt = this.state.environment.time))
                }
            }

            if (finishedMatches.size > 0 || newMatches.size > 0) {
                // If necessary, update state with new situations added and finished situations removed
                this.state = this.state.copy(ongoingSituations = this.state.ongoingSituations
                        // Remove finished situations
                        .filterKeys { rule -> !finishedMatches.contains(rule) }
                        // Add new situations
                        .plus(newMatches.map { rule -> Pair(rule, Situation(
                                rule = rule,
                                startedAt = this.state.environment.time))
                        }))
            }
        })
    }
}
