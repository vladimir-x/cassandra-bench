package ru.dude.cass.check.bench


/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
interface Benchable {

    val name: Processors

    val tableName: String

    fun clear()

    fun beforeInserts(rowCount: Int, benchSet: BenchSet)

    fun insert(id: Long)

    fun afterInserts(rowCount: Int)

    fun select(id: Int)

    fun runBenchInsert(insertCountInt: Int)

    fun runBenchSelect(maxId: Int, selectTimeSec: Int): Int
}
