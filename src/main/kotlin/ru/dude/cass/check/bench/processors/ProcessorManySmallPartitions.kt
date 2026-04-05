package ru.dude.cass.check.bench.processors

import org.springframework.stereotype.Component
import ru.dude.cass.check.bench.ProcessorAbstract
import ru.dude.cass.check.bench.Processors
import ru.dude.cass.check.service.repo.PhoneRepo

/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
@Component
internal class ProcessorManySmallPartitions(private val repo: PhoneRepo) : ProcessorAbstract() {

    override val name = Processors.many_small_partitions

    override val tableName = repo.tableName()

    override fun clear() {
        repo.clearTable()
    }

    override fun insert(id: Long) {
        repo.saveNewEntity(id)
    }

    override fun select(id: Int) {
        repo.findById(id.toLong())
    }
}
