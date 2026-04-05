package ru.dude.cass.check.repository

import com.datastax.oss.driver.api.core.data.CqlVector
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import ru.dude.cass.check.entity.PhoneVectorDP


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */

interface PhoneVectorDPRepository : CassandraRepository<PhoneVectorDP, Long>{

    @Query("truncate table ${PhoneVectorDP.TABLE_NAME}")
    fun clearTable()


    @Query("SELECT * FROM ${PhoneVectorDP.TABLE_NAME}\n" +
            "    ORDER BY comment_vector ANN OF :vector \n" +
            "    LIMIT 1;")
    fun findByVector(vector: CqlVector<Float>): PhoneVectorDP?

}
