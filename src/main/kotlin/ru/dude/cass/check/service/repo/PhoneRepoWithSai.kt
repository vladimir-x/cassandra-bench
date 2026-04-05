package ru.dude.cass.check.service.repo

import org.springframework.stereotype.Service
import ru.dude.cass.check.entity.Phone
import ru.dude.cass.check.entity.Phone.Companion.NAME_IPHONE
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.entity.PhoneSai
import ru.dude.cass.check.repository.PhoneSaiRepository
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

@Service
internal class PhoneRepoWithSai(private val cassRepo: PhoneSaiRepository) : Repo {

    private companion object {
        val r = Random(777)
    }

    override fun tableName(): String {
        return PhoneSai.TABLE_NAME
    }

    override fun clearTable() {
        cassRepo.clearTable()
    }

    override fun saveNewEntity(id: Long): Any {
        return cassRepo.save(
            PhoneSai(
                id = id,
                brand = Phone.BRAND_APPLE,
                name = makeName(id),
                cost_min = r.nextLong(200, 300),
                cost_max = r.nextLong(400, 500)
            )
        )
    }

    fun findByName(name: String): PhoneSai? {
        return cassRepo.findByName(name)
    }

}

