package exercise

import rx.Observable
import rx.subjects.PublishSubject
import java.time.Instant

/**
 * @version $Id$
 */
class Supervisor(vararg val rules: Rule) {

    /**
     * Isolate the event processor mutable state in its own class
     */
    private data class State(
            val environment: Environment,
            val ongoingSituations: Map<Rule, Situation>,
            val pastConditions: Set<Rule>
    )

    private var state = State(
            environment = Environment(Instant.EPOCH, Double.MIN_VALUE, Double.MIN_VALUE),
            ongoingSituations = mapOf(),
            pastConditions = setOf()
    )

    private val subject: PublishSubject<DiseaseCondition> = PublishSubject.create()

    // ---------------------------------------------------------------------------------------------
    // Public API

    val notifications: Observable<DiseaseCondition> = subject

    fun process(measurement: SensorValue): Unit {

        synchronized(state, {
            if (measurement.timestamp.isBefore(state.environment.time)) {
                // Prevent to use with randomly timed sensor values
                throw IllegalStateException("Events are expected to arrive in natural order")
            }

            // Update state with new environment last received sensor value
            this.state = this.state.copy(
                    environment = when (measurement.dimension) {
                        Dimension.TEMPERATURE -> this.state.environment.copy(
                                time = measurement.timestamp,
                                temperature = measurement.value)
                        Dimension.HUMIDITY -> this.state.environment.copy(
                                time = measurement.timestamp,
                                humidity = measurement.value)
                    })

            if (this.ready()) { // Wait until we have both temperature and humidity before evaluating any rule

                val rulesMatching = rules.filter { rule -> rule.evaluate(this.state.environment) }
                val newMatches = rulesMatching.filter { !this.state.ongoingSituations.containsKey(it) }
                val finishedMatches = this.state.ongoingSituations.keys.filter { !rulesMatching.contains(it) }

                finishedMatches.forEach { rule ->
                    val situation= this.state.ongoingSituations[rule]!!
                    if (this.state.environment.time.minusSeconds(rule.duration * 60).isAfter(situation.startedAt)) {
                        subject.onNext(DiseaseCondition(
                                rule = rule,
                                startedAt = situation.startedAt,
                                finishedAt = this.state.environment.time
                        ))
                    }
                }

                val updatedSituations = this.state.ongoingSituations
                        // Remove finished situations
                        .filterKeys { rule -> !finishedMatches.contains(rule) }
                        // Add new situations
                        .plus(newMatches.map { rule -> Pair(rule, Situation(
                                rule = rule,
                                startedAt = this.state.environment.time))
                        })

                this.state = this.state.copy(ongoingSituations = updatedSituations)
            }
        })
    }

    fun ready() = this.state.environment.humidity !== Double.MIN_VALUE
            && this.state.environment.temperature !== Double.MIN_VALUE
}
