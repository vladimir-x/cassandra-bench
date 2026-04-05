package ru.dude.cass.check.repository

import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import ru.dude.cass.check.entity.PhoneByBrandAndName


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */

interface PhoneByNameRepositoryCass : CassandraRepository<PhoneByBrandAndName, String> {


    @Query("truncate table ${PhoneByBrandAndName.TABLE_NAME}")
    fun clearTable()


    @Query("SELECT * FROM ${PhoneByBrandAndName.TABLE_NAME} WHERE brand = :brand AND name = :name LIMIT 1")
    fun findByBrandAndName(brand: String, name: String): PhoneByBrandAndName?

}
