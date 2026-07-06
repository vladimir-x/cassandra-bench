package ru.dude.cass.check.bench

/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
data class BenchSet(
    val enabled: Boolean = false,
    val name: Processors,
    val title: String = "",
    val tag: String = "",
    val insertCount: String = "1_000_000",
    val flushAfterInsert: Boolean = true,
    val compactAfterInsert: Boolean = false,
    val selectTimeSec: Int = 10,
    val retry: Int = 1,
    val retrySelect: Int = 1,
    val options: Map<String, String> = emptyMap(),
){


    val insertSizes = insertCount.split(",").map { it.replace("_","").toInt() }
}
