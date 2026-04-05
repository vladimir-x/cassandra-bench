package ru.dude.cass.check.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import ru.dude.cass.check.entity.PhoneByBrandAndName.Companion.TABLE_NAME


/**
 * @author Vladimir X
 * Date: 24.08.2025
 */
@Table(TABLE_NAME)
class PhoneByBrandAndName(


    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    var brand: String,

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    var name: String,

    var id: Long,

    ) {

    constructor() : this("","", 0L)

    companion object {
        const val TABLE_NAME = "phone_by_brand"
    }
}
