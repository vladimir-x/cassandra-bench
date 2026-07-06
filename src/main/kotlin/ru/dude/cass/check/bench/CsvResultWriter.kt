package ru.dude.cass.check.bench

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * @author Vladimir X
 * Date: 06.07.2026
 */
@Service
internal class CsvResultWriter {


    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")

    lateinit var fileWriter: FileWriter


    @PostConstruct
    fun setUp() {
        fileWriter = FileWriter("result-${dateTimeFormatter.format(LocalDateTime.now())}.csv", Charsets.UTF_8)

        writerHeader()
    }

    private fun writerHeader() {

        val headerSb = StringBuilder()
        headerSb.append(
            "loadType",
            "|",
            "benchName",
            "|",
            "benchTag",
            "|",
            "successesCount",
            "|",
            "errorCount",
            "|",
            "speedPerSec",
            "|",
            "discSize",
            "|",
            "threads",
            "|",
            "cassVer",
            "|",
            "cpuAverage",
            "|",
            "ramAverage",
            "|",
            "ramMax"
        )
        fileWriter.appendLine(headerSb)
        fileWriter.flush()
    }

    fun appendLine(
        loadType: String,
        benchName: Processors,
        benchTag: String,
        successesCount: Int,
        errorCount: Int,
        speedPerSec: Long,
        discSize: Long,
        threads: Int,
        cassVer: String,
        cpuAverage: Double?,
        ramAverage: Double?,
        ramMax: Double?,
    ) {


        val headerSb = StringBuilder()
        headerSb.append(
            loadType,
            "|",
            benchName.name,
            "|",
            benchTag,
            "|",
            successesCount.toString(),
            "|",
            errorCount.toString(),
            "|",
            speedPerSec,
            "|",
            discSize,
            "|",
            threads,
            "|",
            cassVer,
            "|",
            cpuAverage ?: 0,
            "|",
            ramAverage ?: 0,
            "|",
            ramMax ?: 0
        )
        fileWriter.appendLine(headerSb)
        fileWriter.flush()
    }

    @PreDestroy
    fun onClose() {
        fileWriter.close()
    }
}
