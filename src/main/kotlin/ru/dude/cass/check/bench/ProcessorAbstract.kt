package ru.dude.cass.check.bench

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random


/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
abstract class ProcessorAbstract(
) : Benchable {

    private val logger = LoggerFactory.getLogger(ProcessorAbstract::class.java)

    @Value("\${speedtest.multithreads.threads.count}")
    private var threadsCount: Int = 0


    @Value("\${speedtest.executing.stat.enabled}")
    private var statEnabled = true

    @Autowired
    @Qualifier("executor")
    private lateinit var executor: ExecutorService

    @PreDestroy
    fun destroy(){
        executor.shutdownNow()
    }


    private val statSt = AtomicLong(0)
    private val statPrevCnt = AtomicLong(0)
    private val counter = AtomicLong()
    private val counterOf = AtomicLong()


    val formatter = DecimalFormat("#,###", DecimalFormatSymbols.getInstance().also {
        it.groupingSeparator = '_'
    });

    @Scheduled(fixedRateString = "\${speedtest.executing.stat.delay}")
    fun multithreadStat() {
        if (statEnabled && counter.get() > 0) {
            val delayMs = System.currentTimeMillis() - statSt.getAndSet(System.currentTimeMillis())
            val divCnt = counter.get() - statPrevCnt.getAndSet(counter.get())
            logger.info(
                " {} of {} , {} ops/sec ",
                formatter.format(counter),
                formatter.format(counterOf),
                divCnt * 1000 / delayMs
            )
        }

    }


    override fun runBenchInsert(insertCountInt: Int) {

        counter.set(0)
        counterOf.set(insertCountInt.toLong())

        val idCounter = AtomicLong(0)

        execSafetyMultithread({ counter.getAndIncrement() < insertCountInt }) {
            insert(idCounter.getAndIncrement())
        }

        counter.set(0)

    }

    override fun runBenchSelect(maxId: Int, selectTimeSec: Int): Int {


        val end = LocalDateTime.now().plusSeconds(selectTimeSec.toLong())

        counter.set(0)
        counterOf.set(0)

        val r = Random(777)
        execSafetyMultithread({ LocalDateTime.now() <= end }) {
            select(r.nextInt(maxId))
            counter.incrementAndGet()
        }

        val res = counter.get()

        counter.set(0)
        counterOf.set(0)

        return res.toInt()
    }

    private fun execSafetyMultithread(whileTrue: () -> Boolean, func: () -> Unit) {
        val futures = (1..threadsCount).map {
            executor.submit {
                while (whileTrue()) {
                    try {
                        func()
                    } catch (e: InterruptedException) {
                        System.err.println("Exception: ${e.message}")
                        break
                    } catch (e: Exception) {
                        System.err.println("Exception: ${e.message}")
                    }
                }
            }
        }

        futures.forEach { it.get() }
    }
}
