package exercise

import de.vandermeer.asciitable.v2.V2_AsciiTable
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer
import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes
import org.apache.commons.csv.CSVFormat
import java.time.LocalDateTime


object App {

    val eventProcessor = Supervisor(*allRules)

    fun run () {

        val files = setOf(
                Pair(Dimension.HUMIDITY, "export_cavelab.10.humidity_2016-08-04.csv"),
                Pair(Dimension.TEMPERATURE, "export_cavelab.10.temperature_2016-08-04.csv")
        )

        val recordsLists = files.map { file ->
            CSVFormat.DEFAULT.parse(Utils.getReader(file.second)).drop(1) // (skip header line)
                .map { record -> SensorValue(
                        dimension = file.first,
                        timestamp =  parseDateAsInstant(record.get(0)),
                        value = record.get(1).toDouble()
                )}
        }

        val events = recordsLists.flatten().sortedBy { record -> record.timestamp }

        val table = V2_AsciiTable()
        val tableRenderer  = V2_AsciiTableRenderer()
        tableRenderer.setTheme(V2_E_TableThemes.ASC7_LATEX_STYLE_STRONG.get())
        tableRenderer.setWidth(WidthAbsoluteEven(80))

        table.addRule()
        table.addRow("Disease condition", "Started at", "Finished at", "Duration (HH:mm)")
        table.addStrongRule()

        this.eventProcessor.notifications.subscribe { message ->
            table.addRow(
                    message.rule.name,
                    LocalDateTime.ofInstant(message.startedAt, zone).format(dateFormatter),
                    LocalDateTime.ofInstant(message.finishedAt, zone).format(dateFormatter),
                    formatTime(message.finishedAt.epochSecond - message.startedAt.epochSecond)
            )
            table.addRule()
        }

        events.forEach { event -> this.eventProcessor.process(event) }

        println(tableRenderer.render(table))
    }
}

fun main(args: Array<String>) = App.run()