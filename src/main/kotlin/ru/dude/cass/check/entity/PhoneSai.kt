package ru.dude.cass.check.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import ru.dude.cass.check.entity.PhoneSai.Companion.TABLE_NAME


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */
@Table(TABLE_NAME)
class PhoneSai(

    @PrimaryKey
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    var id: Long,

    var brand: String,
    var name: String,
    var cost_min: Long,
    var cost_max: Long

) {

    constructor() : this(0L, "", "", 0L, 0L)

    companion object{
        const val TABLE_NAME = "phone_saindex"
    }
}
