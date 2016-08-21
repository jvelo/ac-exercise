package exercise

import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven
import org.apache.commons.csv.CSVFormat
import java.time.LocalDateTime
import de.vandermeer.asciitable.v2.V2_AsciiTable as Table
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer as TableRenderer
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes as TableThemes

object App {

    val supervisor = Supervisor(*allRules)

    val files = setOf(
            Pair(Dimension.HUMIDITY, "export_cavelab.10.humidity_2016-08-04.csv"),
            Pair(Dimension.TEMPERATURE, "export_cavelab.10.temperature_2016-08-04.csv")
    )

    /**
     * Entry point of the exercise.
     *
     * Loads the lab CSV exports, feeds them to the supervisor and outputs emitted alerts for
     * possible disease development conditions.
     */
    fun run() {

        val recordsLists = files.map { file ->
            CSVFormat.DEFAULT.parse(Utils.getResourceReader(file.second)).drop(1) // (skip header line)
                    .map { record ->
                        SensorValue(dimension = file.first,
                                    timestamp = parseDateAsInstant(record.get(0)),
                                    value = record.get(1).toDouble())
                    }
        }

        val records = recordsLists.flatten().sortedBy { record -> record.timestamp }

        this.printHeader()

        this.supervisor.alerts.subscribe { alert ->
            table.addRow(
                    alert.rule.name,
                    LocalDateTime.ofInstant(alert.startedAt, timeZone).format(dateFormatter),
                    LocalDateTime.ofInstant(alert.finishedAt, timeZone).format(dateFormatter),
                    formatSecondsAsTime(alert.finishedAt.epochSecond - alert.startedAt.epochSecond)
            )
            table.addRule()
        }

        // Feed all values to the supervisor
        records.forEach { record -> this.supervisor.push(record) }

        println(tableRenderer.render(table))
    }

    private fun printHeader() {
        val headerTable = Table()
        headerTable.addRow("""
        Using files:

            ${files.map { "- ${it.first} : ${it.second}" }.joinToString("\n")}"""
        )
        println(tableRenderer.render(headerTable))
    }

    private val table by lazy {
        val table = Table()
        // Add header row
        table.addRule()
        table.addRow("Disease condition", "Started at", "Finished at", "Duration (HH:mm)")
        table.addStrongRule()
        table
    }

    private val tableRenderer by lazy {
        val tableRenderer = TableRenderer()
        tableRenderer.setTheme(TableThemes.ASC7_LATEX_STYLE_STRONG.get())
        tableRenderer.setWidth(WidthAbsoluteEven(80))
        tableRenderer
    }

}

fun main(args: Array<String>) = App.run()