package ru.dude.cass.check.repository

import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.stereotype.Repository
import ru.dude.cass.check.entity.PhoneSai

@Repository
interface PhoneSaiRepository : CassandraRepository<PhoneSai, Long> {


    @Query("truncate table ${PhoneSai.TABLE_NAME}")
    fun clearTable()



    @Query("SELECT * FROM ${PhoneSai.TABLE_NAME} WHERE brand = :brand LIMIT 1")
    fun findByBrand(brand: String): PhoneSai?

    @Query("SELECT * FROM ${PhoneSai.TABLE_NAME} WHERE name = :name LIMIT 1")
    fun findByName(name: String): PhoneSai?

    @Query("SELECT * FROM ${PhoneSai.TABLE_NAME} WHERE cost_min > :minCost AND cost_min < :maxCost LIMIT 1")
    fun findByCostMinRange(minCost: Long, maxCost: Long): PhoneSai?

    @Query("SELECT * FROM ${PhoneSai.TABLE_NAME} WHERE cost_max > :minCost AND cost_max < :maxCost LIMIT 1")
    fun findByCostMaxRange(minCost: Long, maxCost: Long): PhoneSai?
}

