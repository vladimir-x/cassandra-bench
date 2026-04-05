package ru.dude.cass.check.casstool


/**
 * @author Vladimir X
 * Date: 12.03.2026
 */
data class CassandraRunInfo(
    val javaVesion: String,
    val gc: CassandraGC,
    val cassVersion: String,
    val ssTableFormat: String,
) {
    companion object {
        val UNDEFINED = CassandraRunInfo("Undefined", CassandraGC.UNKNOWN, "Undefined", "Undefined")
        val SCYLLA = CassandraRunInfo("-", CassandraGC.UNKNOWN, "Scylla", "Scylla")
    }
}


enum class CassandraGC {
    UNKNOWN, ZGC, G1, CMS;

    companion object {

        fun byPsInfo(psInfo: String): CassandraGC {
            return when {
                psInfo.contains("-XX:+UseZGC") -> ZGC
                psInfo.contains("-XX:+UseG1GC") -> G1
                psInfo.contains("-XX:+UseConcMarkSweepGC") -> CMS
                else -> UNKNOWN
            }
        }
    }
}
