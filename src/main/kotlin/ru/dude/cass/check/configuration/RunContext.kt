package ru.dude.cass.check.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.dude.cass.check.casstool.CassTool


/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Component
class RunContext {

    @Value("\${speedtest.multithreads.threads.count}")
    var threadsCount: Int = 0

    val cassVersion: String
        get() = CassTool.INSTANCE.runInfo.cassVersion

    /**
     * Информация о том, в каком окружении запускаются тесты:
     * версия и настройки кассанды,
     * версия java и GC
     * количество потоков при проведении бенчей
     */
    fun getContextInfo(): String {

        val cassInfo = CassTool.INSTANCE.runInfo
        return "threads: $threadsCount, cass_ver:${cassInfo.cassVersion}, cass_java:${cassInfo.javaVesion}, cass_gc:${cassInfo.gc}, tableFormat: ${cassInfo.ssTableFormat}"
    }
}
