package ru.dude.cass.check.bench.processors

import org.springframework.stereotype.Component
import ru.dude.cass.check.bench.BenchSet
import ru.dude.cass.check.bench.ProcessorAbstract
import ru.dude.cass.check.bench.Processors
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.service.repo.PhoneByNameRepo

/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Component
internal class ProcessorFindBySearchTable(private val repo: PhoneByNameRepo) : ProcessorAbstract() {

    override val name = Processors.find_by_searchtable

    override val tableName = repo.tableName()

    override fun clear() {
        repo.clearTable()
    }

    override fun beforeInserts(rowCount: Int, benchSet: BenchSet) {
    }
    override fun afterInserts(rowCount: Int) {
    }

    override fun insert(id: Long) {
        repo.saveNewPhoneEntity(id)
    }

    override fun select(id: Int) {
        repo.findPhoneByName(makeName(id))
    }
}
