package exercise

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber

/**
 * @version $Id$
 */
class AllTests {

    @get:Rule
    val fixture: FixtureRule = FixtureRule()

    // Oidium sporulation tests --------------------------------------------------------------------

    @Test
    @Fixture("oidium-sporulation/detected-once.csv")
    @Rules(RuleName.OIDIUM_SPORULATION)
    fun test_oidium_sporulation_detected_once() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(1, subscriber.onNextEvents.size)
        Assert.assertEquals(parseDateAsInstant("01/08/2016 00:05:00"),
                subscriber.onNextEvents.first().startedAt)
        Assert.assertEquals(parseDateAsInstant("01/08/2016 01:10:00"),
                subscriber.onNextEvents.first().finishedAt)
    }

    @Test
    @Fixture("oidium-sporulation/detected-twice.csv")
    @Rules(RuleName.OIDIUM_SPORULATION)
    fun test_oidium_sporulation_detected_twice() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(2, subscriber.onNextEvents.size)
    }

    @Test
    @Fixture("oidium-sporulation/detected-never.csv")
    @Rules(RuleName.OIDIUM_SPORULATION)
    fun test_oidium_sporulation_detected_never() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(0, subscriber.onNextEvents.size)
    }

    // Botrytis tests ------------------------------------------------------------------------------

    @Test
    @Fixture("botrytis/detected-once.csv")
    @Rules(RuleName.BOTRYTIS)
    fun test_botrytis_detected_once() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(1, subscriber.onNextEvents.size)
        Assert.assertEquals(parseDateAsInstant("01/08/2016 00:05:00"),
                subscriber.onNextEvents.first().startedAt)
        Assert.assertEquals(parseDateAsInstant("01/08/2016 06:10:00"),
                subscriber.onNextEvents.first().finishedAt)
    }

    @Test
    @Fixture("botrytis/detected-twice.csv")
    @Rules(RuleName.BOTRYTIS)
    fun test_botrytis_detected_twice() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(2, subscriber.onNextEvents.size)
    }

    @Test
    @Fixture("botrytis/detected-never.csv")
    @Rules(RuleName.BOTRYTIS)
    fun test_botrytis_detected_never() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(0, subscriber.onNextEvents.size)
    }

    // Oidium sporulation tests --------------------------------------------------------------------

    @Test
    @Fixture("oidium-development/with-precondition.csv")
    @Rules(RuleName.OIDIUM_DEVELOPMENT, RuleName.OIDIUM_SPORULATION)
    fun test_oidium_development_with_precondition() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(2, subscriber.onNextEvents.size)
        Assert.assertEquals(oidiumSporulation, subscriber.onNextEvents[0].rule)
        Assert.assertEquals(oidiumDevelopment, subscriber.onNextEvents[1].rule)
    }

    @Test
    @Fixture("oidium-development/without-precondition.csv")
    @Rules(RuleName.OIDIUM_DEVELOPMENT, RuleName.OIDIUM_SPORULATION)
    fun test_oidium_development_without_precondition() {
        val subscriber = execute()
        subscriber.assertNoErrors()
        Assert.assertEquals(0, subscriber.onNextEvents.size)
    }

    // Helpers -------------------------------------------------------------------------------------

    private fun execute(): TestSubscriber<Alert> {
        val supervisor = Supervisor(*fixture.rules.toTypedArray())
        val subscriber = TestSubscriber<Alert>()
        supervisor.alerts.subscribe(subscriber)

        fixture.records.forEach { supervisor.push(it) }
        return subscriber
    }
}