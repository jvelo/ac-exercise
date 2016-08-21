package exercise

import rx.Observable
import rx.subjects.PublishSubject
import java.time.Instant

/**
 * The meat of the matter.
 *
 * The supervisor takes [SensorValue] events in (expected to arrive time-ordered), processes them
 * and raises an alert when [a rule][Rule] has been evaluated positively for its given time threshold.
 *
 * Sample usage :
 *
 * ```
 * val supervisor = Supervisor(rule1, rule2, rule3)
 * supervisor.alerts.subscribe { alert -> println ("An alert has been raised ! $alert") }
 * supervisor.push(sensorValue1)
 * supervisor.push(sensorValue2)
 * supervisor.push(sensorValue3)
 * // etc.
 * ```
 *
 * @property rules the list of rules to evaluate for sending alerts.
 */
class Supervisor(vararg val rules: Rule) {

    /**
     * Isolated mutable state structure
     *
     * @property environment the current (last seen timestamp) grow-house environment
     * @property ongoingSituations potential
     */
    private data class State(
            val environment: Environment,
            val ongoingSituations: Map<Rule, Instant>,
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

    // The alert subject exposed as an observable
    val alerts: Observable<Alert> = subject

    /**
     * Feed a new measurement (sensor value) to the supervisor.
     *
     * @param measurement the sensor value to push to the supervisor
     */
    @Synchronized
    fun push(measurement: SensorValue) {

        // Update state with new environment last received sensor value
        updateEnvironment(measurement)

        // Find rules matching the current environment ...
        val rulesMatching = rules.filter { rule ->
            rule.preconditions.all { this.state.pastAlerts.contains(it) } && rule.evaluate(this.state.environment)
        }

        // Extract the rules that are new (were not matching before)
        val newMatches = rulesMatching.filter { !this.state.ongoingSituations.containsKey(it) }

        // ... and those that were matching but don't anymore
        val finishedMatches = this.state.ongoingSituations.keys.filter { !rulesMatching.contains(it) }

        finishedMatches.forEach { rule ->
            val startedAt = this.state.ongoingSituations[rule]!!
            if (this.state.environment.time.minusSeconds(rule.duration * 60).isAfter(startedAt)) {
                // Threshold passed ! This is a serious situation, emit an alert
                doEmitAlert(rule, startedAt)
            }
        }

        // If necessary, update state with new situations added and finished situations removed
        updateSituation(finishedMatches, newMatches)
    }

    // ---------------------------------------------------------------------------------------------
    // Private helpers

    private fun updateSituation(finishedMatches: List<Rule>, newMatches: List<Rule>) {
        if (finishedMatches.size > 0 || newMatches.size > 0) {
            this.state = this.state.copy(ongoingSituations = this.state.ongoingSituations
                    // Remove finished situations
                    .filterKeys { rule -> !finishedMatches.contains(rule) }
                    // Add new situations
                    .plus(newMatches.map { rule -> Pair(rule, this.state.environment.time) }))
        }
    }

    private fun updateEnvironment(measurement: SensorValue) {
        if (measurement.timestamp.isBefore(state.environment.time)) {
            // Prevent to use with randomly timed sensor values
            subject.onError(IllegalStateException("Events are expected to arrive in natural order"))
        }

        this.state = this.state.copy(environment = Environment(
                time = measurement.timestamp,
                conditions = this.state.environment.conditions.plus(
                        Pair(measurement.dimension, measurement.value))
        ))
    }

    private fun doEmitAlert(rule: Rule, startedAt: Instant) {
        subject.onNext(Alert(
                rule = rule,
                startedAt = startedAt,
                finishedAt = this.state.environment.time))

        // Update state adding this alert to the set of past alerts
        this.state = this.state.copy(pastAlerts = this.state.pastAlerts.plus(rule))
    }
}
