package exercise

import org.apache.commons.csv.CSVFormat
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Fixture(val file: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Rules(vararg val rules: RuleName)

enum class RuleName {
    OIDIUM_SPORULATION,
    BOTRYTIS,
    OIDIUM_DEVELOPMENT
}

class FixtureRule : TestRule {

    private var _records: List<SensorValue> = listOf()
    private var _rules: List<Rule> = listOf()

    val records: List<SensorValue>
        get() = this._records

    val rules: List<Rule>
        get() = this._rules

    override fun apply(base: Statement?, description: Description?): Statement =
            object : Statement() {
                override fun evaluate() {

                    val fixture: Fixture? = description?.getAnnotation(Fixture::class.java)
                    if (fixture != null) {
                        _records = loadTestFile("fixtures/${fixture.file}")
                    }

                    val rules: Rules? = description?.getAnnotation(Rules::class.java)
                    if (rules != null) {
                        _rules = rules.rules.map { ruleName ->
                            when (ruleName) {
                                RuleName.OIDIUM_SPORULATION -> oidiumSporulation
                                RuleName.BOTRYTIS -> botrytis
                                RuleName.OIDIUM_DEVELOPMENT -> oidiumSporulation
                            }
                        }
                    }

                    base?.evaluate()
                }
            }

    private fun loadTestFile(name: String): List<SensorValue> {
        val records = CSVFormat.DEFAULT.parse(Utils.getReader(name)).drop(1) // (skip header line)
                .map { record ->
                    listOf(
                            SensorValue(
                                    dimension = Dimension.HUMIDITY,
                                    timestamp = parseDateAsInstant(record.get(0)),
                                    value = record.get(1).toDouble()
                            ),
                            SensorValue(
                                    dimension = Dimension.TEMPERATURE,
                                    timestamp = parseDateAsInstant(record.get(0)),
                                    value = record.get(2).toDouble()
                            )
                    )
                }
        return records.flatten().sortedBy { it.timestamp }
    }

}