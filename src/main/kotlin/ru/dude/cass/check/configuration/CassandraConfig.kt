package ru.dude.cass.check.configuration

import com.datastax.oss.driver.shaded.guava.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */
@Configuration
@EnableCassandraRepositories(
    basePackages = ["ru.dude.cass.check.repository"]
)
@EnableScheduling
internal class CassandraConfig {

    @Value("\${speedtest.multithreads.threads.count}")
    private var threadsCount: Int = 0

    @Bean(name = ["coroutineDisp"], destroyMethod = "close")
    fun coroutineDisp(): ExecutorCoroutineDispatcher {
        return Executors.newFixedThreadPool(threadsCount, ThreadFactoryBuilder().setNameFormat("cor-%d").build())
            .asCoroutineDispatcher()
    }

    @Bean(name = ["executor"], destroyMethod = "shutdownNow")
    fun executor(): ExecutorService {
        return Executors.newFixedThreadPool(threadsCount)
    }

    @Bean(name = ["sentinelExecutor"], destroyMethod = "shutdownNow")
    fun sentinelExecutor(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("sentinel-%d").build())
    }
}
