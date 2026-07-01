package ru.dude.cass.check.casstool


/**
 * @author Vladimir X
 * Date: 12.03.2026
 */
data class CassandraRunInfo(
    val javaVesion: String,
    val gc: CassandraGC,
    val xmx: String,
    val cassVersion: String,
    val ssTableFormat: String,
) {
    companion object {
        val UNDEFINED = CassandraRunInfo("Undefined", CassandraGC.UNKNOWN, "Undefined","Undefined", "Undefined")
        fun SCYLLA(srvVersion: String) = CassandraRunInfo("-", CassandraGC.UNKNOWN, "Undefined", srvVersion, "Scylla")

        fun UNKNOWN(srvVersion: String) = CassandraRunInfo("Unknown", CassandraGC.UNKNOWN, "Undefined", srvVersion, "Unknown")

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
