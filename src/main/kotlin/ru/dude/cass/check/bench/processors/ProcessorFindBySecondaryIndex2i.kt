package ru.dude.cass.check.bench.processors

import org.springframework.stereotype.Component
import ru.dude.cass.check.bench.ProcessorAbstract
import ru.dude.cass.check.bench.Processors
import ru.dude.cass.check.entity.Phone.Companion.makeName
import ru.dude.cass.check.service.repo.PhoneRepoWithRsi

/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Component
internal class ProcessorFindBySecondaryIndex2i(private val repo: PhoneRepoWithRsi) : ProcessorAbstract() {

    override val name = Processors.find_by_secondary_index

    override val tableName = repo.tableName()

    override fun clear() {
        repo.clearTable()
    }

    override fun insert(id: Long) {
        repo.saveNewEntity(id)
    }

    override fun select(id: Int) {
        repo.findByName(makeName(id))
    }
}
