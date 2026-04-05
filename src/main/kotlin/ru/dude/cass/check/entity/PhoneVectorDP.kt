package ru.dude.cass.check.entity

import com.datastax.oss.driver.api.core.data.CqlVector
import org.springframework.data.annotation.Id
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.core.mapping.VectorType
import ru.dude.cass.check.entity.PhoneVectorDP.Companion.TABLE_NAME


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */
@Table(TABLE_NAME)
class PhoneVectorDP(

    @PrimaryKey
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    var id: Long,

    var brand: String,
    var name: String,
    var cost_min: Long,
    var cost_max: Long,



    @Column("comment_vector")
    @VectorType(dimensions = 128)
    var comment_vector: CqlVector<Float>,
    //var comment_vector: FloatArray,



    ) {

    constructor() : this(0L, "", "", 0L, 0L, CqlVector.newInstance())

    companion object{

        const val TABLE_NAME = "phone_vector_dp"

    }

}
