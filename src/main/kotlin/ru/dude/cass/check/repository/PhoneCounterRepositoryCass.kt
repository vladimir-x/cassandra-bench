package ru.dude.cass.check.repository

import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import ru.dude.cass.check.entity.Phone
import ru.dude.cass.check.entity.PhoneCounter


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */

interface PhoneCounterRepositoryCass : CassandraRepository<PhoneCounter, Long>{

    @Query("truncate table ${PhoneCounter.TABLE_NAME}")
    fun clearTable()

    @Query("UPDATE ${PhoneCounter.TABLE_NAME} SET counter = :newCounter, version = :newVersion WHERE id = :id IF version = :oldVersion")
    fun updateByLWT(id: Long, newCounter: Long, oldVersion: Long, newVersion: Long): Boolean

}
