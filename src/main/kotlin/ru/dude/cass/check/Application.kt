package ru.dude.cass.check

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.dude.cass.check.bench.BenchRunner
import ru.dude.cass.check.bench.BenchSetProperties
import ru.dude.cass.check.casstool.CassTool

@SpringBootApplication
@EnableConfigurationProperties(BenchSetProperties::class)
internal class Application(
    private val benchSetProperties: BenchSetProperties,
    private val benchRunner: BenchRunner,
) : CommandLineRunner {


    override fun run(vararg args: String?) {

        benchSetProperties.benches.values.filter { it.enabled }.forEach { bs ->
            benchRunner.runOneSet(bs)
        }
    }

}

fun main(args: Array<String>) {

    val cassTool = CassTool.INSTANCE

    if (cassTool.config.startEnable) {
        cassTool.cassStart()
    }


    if (cassTool.config.createKeyspaceEnable) {
        cassTool.createKeyspace()
    }

    cassTool.cassCollectInfo()


    try {
        val context = runApplication<Application>(*args)
        SpringApplication.exit(context)

    } finally {

        if (cassTool.config.stopEnable) {
            cassTool.cassStop()
        }
    }
}
