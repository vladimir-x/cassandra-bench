package ru.dude.cass.check.service.repo

import org.springframework.stereotype.Service
import ru.dude.cass.check.entity.Phone
import ru.dude.cass.check.entity.Phone.Companion.NAME_IPHONE
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.entity.PhoneCounter
import ru.dude.cass.check.repository.PhoneCounterRepositoryCass
import ru.dude.cass.check.repository.PhoneRepositoryCass
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random


/**
 * @author Vladimir X
 * Date: 07.09.2025
 */
@Service
internal class PhoneCounterRepo (private val cassRepo: PhoneCounterRepositoryCass) : Repo {

    private companion object{
        val r = Random(777)
    }

    override fun tableName(): String {
        return PhoneCounter.TABLE_NAME
    }

    override fun clearTable() {
        cassRepo.clearTable()
    }

    override fun saveNewEntity(id: Long): Any {
        return cassRepo.save(PhoneCounter(id = id, Phone.BRAND_APPLE, 0, 0))
    }

    fun incrementByLwtEntity(id: Long): Boolean {
        val existed = cassRepo.findById(id).getOrNull() ?: return false
        val res = cassRepo.updateByLWT(id, existed.counter + 10, existed.version, existed.version + 1)
        return res
    }

    fun findById(id: Long): Any? {
        return cassRepo.findById(id).getOrNull()
    }
}
