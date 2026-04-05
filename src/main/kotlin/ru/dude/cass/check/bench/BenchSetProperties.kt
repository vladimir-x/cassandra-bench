package ru.dude.cass.check.bench

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Configuration
@ConfigurationProperties(prefix = "speedtest")
class BenchSetProperties {

    var benches: Map<String, BenchSet> = mutableMapOf()
}
