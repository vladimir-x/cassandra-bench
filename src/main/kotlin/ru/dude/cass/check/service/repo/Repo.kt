package ru.dude.cass.check.service.repo

/**
 * @author Vladimir X
 * Date: 07.09.2025
 */
interface Repo {

    fun tableName(): String

    fun clearTable()

    fun saveNewEntity(id: Long): Any

}
