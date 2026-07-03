package ru.dude.cass.check.bench.processors

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.dude.cass.check.bench.BenchSet
import ru.dude.cass.check.bench.ProcessorAbstract
import ru.dude.cass.check.bench.Processors
import ru.dude.cass.check.service.repo.PhoneCounterRepo
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Component
internal class ProcessorLwt(private val repo: PhoneCounterRepo) : ProcessorAbstract() {

    override val name = Processors.lwt_bench

    override val tableName = repo.tableName()

    private val repeatCounter = AtomicLong(0)

    @Volatile
    private var usedPartitions: Long = 0

    private val rand: Random = Random(777)


    private val logger = LoggerFactory.getLogger(ProcessorLwt::class.java)

    override fun clear() {
        repo.clearTable()
    }

    override fun beforeInserts(rowCount: Int, benchSet: BenchSet) {
        usedPartitions = benchSet.options.getOrDefault("used_partitions", "1").replace("_","").toLong()

        for (i in 1..usedPartitions) {
            repo.saveNewEntity(i)
        }

    }

    override fun insert(id: Long) {

        val rid = rand.nextLong(usedPartitions) + 1

        while (!repo.incrementByLwtEntity(rid)){
            repeatCounter.incrementAndGet()
        }
    }

    override fun afterInserts(rowCount: Int) {
        logger.info("Lwt strong concurrency for {}, repeatCounter: {}", rowCount,repeatCounter)
    }

    override fun select(id: Int) {
        repo.findById(id.toLong())
    }
}
