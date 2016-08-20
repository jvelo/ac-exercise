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
 * The whole grow-house environment : temperature and humidity at a given time
 */
data class Environment(val time: Instant, val temperature: Double, val humidity: Double)

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
 */
data class Rule(val name: String, val duration: Long, val expressions: List<Expression>) {

    /**
     * Evaluate this rule against the passed environment.
     *
     * @return true if the rule is positive against the passed conditions, false otherwise
     */
    fun evaluate(environment: Environment): Boolean {
        return expressions.all { expression ->
            val evaluated = when (expression.dimension) {
                Dimension.TEMPERATURE -> environment.temperature
                Dimension.HUMIDITY -> environment.humidity
            }
            return@all when (expression.operator) {
                Operator.GREATER_THAN -> evaluated > expression.value
                Operator.LESSER_THAN -> evaluated < expression.value
            }
        }
    }
}

/**
 *
 */
data class DiseaseCondition(val rule: Rule, val startedAt: Instant, val finishedAt: Instant)

/**
 * Potential disease condition, started at a given time for a given rule. It becomes an actual
 * disease condition when/if it is sustained for the rule threshold time
 */
data class Situation(val startedAt: Instant, val rule: Rule)