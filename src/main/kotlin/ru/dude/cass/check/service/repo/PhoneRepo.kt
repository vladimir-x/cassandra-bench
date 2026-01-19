package ru.dude.cass.check.service.repo

import org.springframework.stereotype.Service
import ru.dude.cass.check.entity.Phone
import ru.dude.cass.check.entity.Phone.Companion.NAME_IPHONE
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.repository.PhoneRepositoryCass
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random


/**
 * @author Vladimir X
 * Date: 07.09.2025
 */
@Service
internal class PhoneRepo (private val cassRepo: PhoneRepositoryCass) : Repo {

    private companion object{
        val r = Random(777)
    }

    override fun tableName(): String {
        return Phone.TABLE_NAME
    }

    override fun clearTable() {
        cassRepo.clearTable()
    }

    override fun saveNewEntity(id: Long): Any {
        return cassRepo.save(
            Phone(
                id = id, Phone.BRAND_APPLE, makeName(id),
                r.nextLong(200, 300),
                r.nextLong(400, 500)
            )
        )
    }

    fun findById(id: Long): Any? {
        return cassRepo.findById(id).getOrNull()
    }
}
