package exercise

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * @version $Id$
 */
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
val zone: ZoneId = ZoneId.of("Europe/Paris")

fun parseDateAsInstant(dateAsString: String): Instant {
    val date = LocalDateTime.parse(dateAsString, dateFormatter)
    return date.toInstant(zone.rules.getOffset(date))
}

fun formatTime(seconds: Long) = String.format("%02d:%02d",
        TimeUnit.SECONDS.toHours(seconds),
        TimeUnit.SECONDS.toMinutes(seconds) % 60)

object Utils {
    fun getReader(fileName: String): Reader =
            BufferedReader(InputStreamReader(this.javaClass.classLoader.getResourceAsStream(fileName)))
}
