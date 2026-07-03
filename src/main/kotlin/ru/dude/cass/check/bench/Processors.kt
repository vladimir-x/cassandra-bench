package ru.dude.cass.check.bench


/**
 * @author Vladimir X
 * Date: 16.02.2026
 */
enum class Processors {
    many_small_partitions,
    one_big_partition,
    find_by_searchtable,
    find_by_secondary_index,
    find_by_sai,
    find_by_sai_vector,
    lwt_bench,
}
