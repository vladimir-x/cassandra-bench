package ru.dude.cass.check.bench

import ru.dude.cass.check.tracker.CpuTrackerClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.dude.cass.check.casstool.CassTool
import ru.dude.cass.check.configuration.RunContext
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.max


/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Component
internal class BenchRunner(
    private val runContext: RunContext,
    private val benchProcessors: List<Benchable>,
    private val cpuTracker: CpuTrackerClient,
    private val csvResultWriter: CsvResultWriter,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(BenchRunner::class.java)
        private val summaryLogger = LoggerFactory.getLogger("summaryLogger")
    }


    fun runOneSet(benchSet: BenchSet) {


        // нашли нужный бенч-процессор
        val benchProcessor = benchProcessors.firstOrNull { it.name == benchSet.name }
            ?: throw RuntimeException("No bench processor ${benchSet.name} found")

        for (i in 1..benchSet.retry) {

            benchSet.insertSizes.forEach { size ->
                runOneTryInsert(benchProcessor, benchSet, size, i)

                for (j in 1..benchSet.retrySelect) {
                    runOneTrySelect(benchProcessor, benchSet, size, j)
                }
            }
        }

    }

    private fun runOneTryInsert(benchProcessor: Benchable, benchSet: BenchSet, rowCount: Int, tryCount: Int) {

        val contextInfo = runContext.getContextInfo()
        logger.info("Running [{}], retry {} of {}. Inserts count: {}", benchSet.name, tryCount, benchSet.retry, rowCount)

        benchProcessor.clear()

        logger.info("Data cleared for [{}]", benchSet.name)

        benchProcessor.beforeInserts(rowCount, benchSet)

        cpuTracker.startMeasurement()


        val st = LocalDateTime.now()


        val processRes = benchProcessor.runBenchInsert(rowCount)

        val d = Duration.between(st, LocalDateTime.now())

        val cpuMetrics = cpuTracker.stopMeasurement()

        benchProcessor.afterInserts(rowCount)


        if (benchSet.flushAfterInsert) {
            CassTool.INSTANCE.flushAndCompact(benchProcessor.tableName)
        }

        val discSize = CassTool.INSTANCE.tableOnDiscSizeMb(benchProcessor.tableName)

        val speed = processRes.successedCount / max(1, d.toSeconds())
        summaryLogger.info(
            "Bench complete | inserts | name: [{}]\t| tag: [{}]\t| rowCount: {}\t| speed: {}\tins/sec\t| discSize: {}\tMB |  threads: {}\t |  cass_ver: {}\t  |  cpu_average: {}\t|  ram_average: {}\t | ram_max: {}\t | context: {}",
            benchSet.name,
            benchSet.tag,
            rowCount,
            speed,
            discSize,
            runContext.threadsCount,
            runContext.cassVersion,
            cpuMetrics?.cpuAverageTotal,
            cpuMetrics?.ramAverageMb,
            cpuMetrics?.ramAverageMb,
            contextInfo
        )

        csvResultWriter.appendLine(
            "inserts",
            benchSet.name,
            benchSet.tag,
            processRes.successedCount,
            processRes.errorCount,
            speed,
            discSize,
            runContext.threadsCount,
            runContext.cassVersion,

            cpuMetrics?.cpuAverageTotal,
            cpuMetrics?.ramAverageMb,
            cpuMetrics?.ramAverageMb,
        )

    }

    private fun runOneTrySelect(benchProcessor: Benchable, benchSet: BenchSet, rowCount: Int, tryCount: Int) {

        logger.info("Running [{}], select-retry {} of {} ", benchSet.name, tryCount, benchSet.retrySelect)


        cpuTracker.startMeasurement()

        val st = LocalDateTime.now()

        val processRes = benchProcessor.runBenchSelect(rowCount, benchSet.selectTimeSec)

        val d = Duration.between(st, LocalDateTime.now())
        val speed = processRes.successedCount / max(1, d.toSeconds())

        val cpuMetrics = cpuTracker.stopMeasurement()

        summaryLogger.info(
            "Bench complete | selects | name: [{}]\t| rowCount: {}\t| speed: {}\tsel/sec\t| context: {}",
            benchSet.name,
            rowCount,
            speed,
            runContext.getContextInfo()
        )


        csvResultWriter.appendLine(
            "selects",
            benchSet.name,
            benchSet.tag,

            processRes.successedCount,
            processRes.errorCount,
            speed,
            0,
            runContext.threadsCount,
            runContext.cassVersion,

            cpuMetrics?.cpuAverageTotal,
            cpuMetrics?.ramAverageMb,
            cpuMetrics?.ramAverageMb,
        )


    }
}
