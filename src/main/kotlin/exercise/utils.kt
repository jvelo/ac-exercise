package exercise

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

// Default formatter for dates as exported in from the lab
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

// Assumed time-zone of the measurements
val timeZone: ZoneId = ZoneId.of("Europe/Paris")

/**
 * Parses a date expressed in the [dateFormatter] format at the [timeZone] to an instant
 */
fun parseDateAsInstant(dateAsString: String): Instant {
    val date = LocalDateTime.parse(dateAsString, dateFormatter)
    return date.toInstant(timeZone.rules.getOffset(date))
}

/**
 * Formats an amount of seconds in a human readable time format
 */
fun formatSecondsAsTime(seconds: Long) = String.format("%02d:%02d",
        TimeUnit.SECONDS.toHours(seconds),
        TimeUnit.SECONDS.toMinutes(seconds) % 60)

object Utils {
    /**
     * Gets a reader a classpath resource with the passed name.
     */
    fun getResourceReader(fileName: String): Reader =
            BufferedReader(InputStreamReader(this.javaClass.classLoader.getResourceAsStream(fileName)))
}

