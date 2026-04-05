package ru.dude.cass.check.service.repo

import com.datastax.oss.driver.api.core.data.CqlVector
import org.springframework.stereotype.Service
import ru.dude.cass.check.entity.Phone.Companion.BRAND_APPLE
import ru.dude.cass.check.entity.Phone.Companion.NAME_IPHONE
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.entity.PhoneVectorDP
import ru.dude.cass.check.repository.PhoneVectorDPRepository
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random


/**
 * @author Vladimir X
 * Date: 07.09.2025
 */
@Service
internal class PhoneRepoVectorDP(private val cassRepo: PhoneVectorDPRepository) : Repo {

    private companion object {
        val r = Random(777)
    }

    override fun tableName(): String {
        return PhoneVectorDP.TABLE_NAME
    }

    override fun clearTable() {
        cassRepo.clearTable()
    }

    override fun saveNewEntity(id: Long): PhoneVectorDP {
        return cassRepo.save(
            PhoneVectorDP(
                id = id, BRAND_APPLE, makeName(id),
                r.nextLong(200, 300),
                r.nextLong(400, 500),
                CqlVector.newInstance(randomVector())
            )
        )
    }


    fun randomVector(): List<Float> {
        return (0 until 128).map { r.nextFloat() }
    }

    fun findByRandomVector(): Any? {
        return cassRepo.findByVector(CqlVector.newInstance(randomVector()))
    }
}
