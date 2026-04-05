package ru.dude.cass.check.service.repo

import org.springframework.stereotype.Service
import ru.dude.cass.check.entity.Phone
import ru.dude.cass.check.entity.Phone.Companion.BRAND_APPLE
import ru.dude.cass.check.entity.Phone.Companion.NAME_IPHONE
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.entity.PhoneByBrandAndName
import ru.dude.cass.check.repository.PhoneByNameRepositoryCass
import ru.dude.cass.check.repository.PhoneRepositoryCass
import kotlin.jvm.optionals.getOrNull


/**
 * @author Vladimir X
 * Date: 07.09.2025
 */
@Service
internal class PhoneByNameRepo(
    private val cassRepo: PhoneByNameRepositoryCass,
    private val phoneRepoCass: PhoneRepositoryCass,
    private val phoneRepo: PhoneRepo,
) : Repo {


    override fun tableName(): String {
        return PhoneByBrandAndName.TABLE_NAME
    }

    override fun clearTable() {
        phoneRepo.clearTable()
        cassRepo.clearTable()
    }

    /**
     * Добавление только в одну партицию с кластерным индексом
     */
    override fun saveNewEntity(id: Long): Any {
        return cassRepo.save(PhoneByBrandAndName(BRAND_APPLE, makeName(id), id))
    }


    /**
     * Поиск только в кластерном индексе
     */
    fun findIdByName(name: String): Any? {
        return cassRepo.findByBrandAndName(BRAND_APPLE, name)
    }

    // для поиска по табличке поиска

    /**
     * Добавление в кластерный и в основную таблицу с данными
     */
    fun saveNewPhoneEntity(id: Long): Any {
        val phone = phoneRepo.saveNewEntity(id) as Phone
        return cassRepo.save(PhoneByBrandAndName(phone.brand, phone.name, id))
    }

    /**
     * Найти данные по кластерному ключу и id из основной таблицы
     */
    fun findPhoneByName(name: String): Any? {
        val byBrand = cassRepo.findByBrandAndName(BRAND_APPLE, name)
        return byBrand?.id?.let { phoneRepoCass.findById(it).getOrNull() }

    }

}
