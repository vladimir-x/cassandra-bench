package ru.dude.cass.check.repository

import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import ru.dude.cass.check.entity.Phone


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */

interface PhoneRepositoryCass : CassandraRepository<Phone, Long>{

    @Query("truncate table ${Phone.TABLE_NAME}")
    fun clearTable()

}
