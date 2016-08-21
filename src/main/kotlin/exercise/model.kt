package exercise

import java.time.Instant

/**
 * The observed dimensions in the grow house
 */
enum class Dimension {
    TEMPERATURE, HUMIDITY
}

/**
 * A value reported by a sensor, for a given [Dimension] at a specific timestamp
 */
data class SensorValue(val timestamp: Instant, val dimension: Dimension, val value: Double)

/**
 * The whole grow-house environment: conditions at a given time
 */
data class Environment(val time: Instant, val conditions: Map<Dimension, Double>)

/**
 * Comparison operators for expressing [rules][Rule]
 */
enum class Operator {
    GREATER_THAN,
    LESSER_THAN
}

/**
 * An expression that can be evaluated as part of a rule
 *
 * @property dimension the dimension of the value to evaluate (i.e. temperature or humidity)
 * @property operator the operator to evaluate against
 * @property value the minimum or maximum value to evaluate against
 */
data class Expression(val dimension: Dimension, val operator: Operator, val value: Double)

/**
 * A rule for describing conditions opportune for the development of a disease
 * @property name the human-readable name of this rule
 * @property duration the time the rule's expression must be valid for for an alert to be raised for
 *  that rule
 * @property expressions the list of expressions to evaluate. A logic AND is assumed : all expressions
 * must return true for the rule to apply
 * @property preconditions a list of rules for which alerts must of occurred before this rule can
 * raise an alert itself
 */
data class Rule(
        val name: String,
        val duration: Long,
        val expressions: List<Expression>,
        val preconditions: List<Rule> = listOf()) {

    /**
     * Evaluate this rule against the passed environment.
     *
     * @return true if the rule is positive against the passed conditions, false otherwise
     */
    fun evaluate(environment: Environment): Boolean {
        return expressions.all { expression ->
            val evaluated = environment.conditions[expression.dimension]
            if (evaluated == null)
                false
            else when (expression.operator) {
                Operator.GREATER_THAN -> evaluated > expression.value
                Operator.LESSER_THAN -> evaluated < expression.value
            }
        }
    }
}

/**
 * An alert, emitted when a rule has been positively evaluated for its threshold duration
 */
data class Alert(val rule: Rule, val startedAt: Instant, val finishedAt: Instant)
