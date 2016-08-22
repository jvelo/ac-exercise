package exercise

import de.vandermeer.asciitable.v2.render.WidthAbsoluteEven
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.csv.CSVFormat
import java.io.StringReader
import java.time.LocalDateTime
import de.vandermeer.asciitable.v2.V2_AsciiTable as Table
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer as TableRenderer
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes as TableThemes

object App {

    // CLI options
    private val options by lazy {
        val options = Options()
        options.addOption(Option.builder().longOpt("temperature").desc("URI of temperature log file")
                .hasArg().argName("URI").build())
        options.addOption(Option.builder().longOpt("humidity").desc("URI of humidity log file")
                .hasArg().argName("URI").build())
        options.addOption(Option.builder().longOpt("help").desc("Prints this message").build())
        options
    }

    // Results table with header line
    private val table by lazy {
        val table = Table()
        // Add header row
        table.addRule()
        table.addRow("Disease condition", "Started at", "Finished at", "Duration (HH:mm)")
        table.addStrongRule()
        table
    }

    // Table renderer
    private val tableRenderer by lazy {
        val tableRenderer = TableRenderer()
        tableRenderer.setTheme(TableThemes.ASC7_LATEX_STYLE_STRONG.get())
        tableRenderer.setWidth(WidthAbsoluteEven(80))
        tableRenderer
    }

    // Our supervisor !
    private val supervisor = Supervisor(*allRules)

    /**
     * Entry point of the exercise.
     *
     * Loads the lab CSV exports, feeds them to the supervisor and outputs emitted alerts for
     * possible disease development conditions.
     */
    fun run(vararg args: String) {

        // Parse the command line arguments
        val parser = DefaultParser()
        val line = parser.parse(options, args)

        if (line.hasOption("help")) {
            val formatter = HelpFormatter()
            formatter.printHelp("ac-exercise", options);
            return
        }

        // Get lab log files to use according to CLI argument with fall back on default files
        val temperatureUri: String? = if (line.hasOption("temperature")) line.getOptionValue("temperature") else null
        val humidityUri: String? = if (line.hasOption("humidity")) line.getOptionValue("humidity") else null
        val logFiles = setOf(
                LogFile(dimension = Dimension.HUMIDITY,
                        location = humidityUri ?: "export_cavelab.10.humidity_2016-08-04.csv",
                        source = if (humidityUri != null) Source.URI else Source.CLASSPATH_RESOURCE),
                LogFile(dimension = Dimension.TEMPERATURE,
                        location = temperatureUri ?: "export_cavelab.10.temperature_2016-08-04.csv",
                        source = if (temperatureUri != null) Source.URI else Source.CLASSPATH_RESOURCE)
        )

        // Load records from CSV log files
        val recordsLists = try {
            logFiles.map { file ->
                val reader = when (file.source) {
                    Source.URI -> StringReader(getRemoteLogFile(file.location))
                    else -> Utils.getResourceReader(file.location)
                }
                CSVFormat.DEFAULT.parse(reader).drop(1) // (skip header line)
                        .map { record ->
                            SensorValue(dimension = file.dimension,
                                    timestamp = parseDateAsInstant(record.get(0)),
                                    value = record.get(1).toDouble())
                        }
            }
        } catch (e: Exception) {
            println("Failed to fetch or parse log file: ${e.message}")
            return
        }

        val records = recordsLists.flatten().sortedBy { record -> record.timestamp }

        this.printHeader(logFiles)

        // Subscribe to alerts reported by the supervisor
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

        // Output the built table with detected diseases
        println(tableRenderer.render(table))
    }

    private fun printHeader(files: Set<LogFile>) {
        val headerTable = Table()
        headerTable.addRow("""
        Using log files:

            ${files.map { "- ${it.dimension} : ${it.location}" }.joinToString("\n")}"""
        )
        println(tableRenderer.render(headerTable))
    }
}

fun main(args: Array<String>) = App.run(*args)