package ru.dude.cass.check.casstool

import ru.dude.cass.check.casstool.CassTool.CassVersion
import java.util.Properties


/**
 * @author Vladimir X
 * Date: 01.07.2026
 */
data class CassPathConfig(
    val cassVersion: CassVersion,
    val cassHost: String,
    val cassPort: String,

    val cqlshWorkPath: String,
    val cqlshBinPath: String,

    val nodetoolWorkPath: String,
    val nodetoolBinPath: String,
    val nodetoolPort: String, // nodetool и jmx

    val binDirPath: String,
    val dataDirPath: String,
) {
    companion object {
        fun byProperties(properties: Properties): CassPathConfig {



            val cassVersion = CassVersion.valueOf(properties.getProperty("casstool.cassandra.enable-version"))


            val cassHost = properties.getProperty("spring.cassandra.contact-points").split(',').map(String::trim).first().split(":")[0]
            val cassPort = properties.getProperty("spring.cassandra.contact-points").split(',').map(String::trim).first().split(":")[1]

            val cassPath = when (cassVersion) {
                CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.path")
                CassVersion.CASS_5 -> properties.getProperty("casstool.cassandra5.path")
                CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.path")
            }

            val binDirPath = cassPath + "/bin"

            val cqlshBinPath = when (cassVersion) {
                CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.cqlsh.path", binDirPath +"/cqlsh")
                CassVersion.CASS_5 -> properties.getProperty("casstool.cassandra5.cqlsh.path", binDirPath +"/cqlsh")
                CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.cqlsh.path",binDirPath +"/cqlsh")
            }

            val cqlshWorkPath = cqlshBinPath.substringBeforeLast("/")

            val nodetoolBinPath = when (cassVersion) {
                CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.nodetool.path", binDirPath +"/nodetool")
                CassVersion.CASS_5 -> properties.getProperty("casstool.cassandra5.nodetool.path", binDirPath +"/nodetool")
                CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.nodetool.path",binDirPath +"/nodetool")
            }


            val nodetoolWorkPath = nodetoolBinPath.substringBeforeLast("/")

            val nodetoolPort = when (cassVersion) {
                CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.nodetool.port", "7199")
                CassVersion.CASS_5 -> properties.getProperty("casstool.cassandra5.nodetool.port", "7199")
                CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.nodetool.port","10000")
            }

            val dataDirPath = when (cassVersion) {
                CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.data.path")
                CassVersion.CASS_5 -> properties.getProperty("casstool.cass5.data.path")
                CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.data.path")
            }

            return CassPathConfig(
                cassVersion,
                cassHost,
                cassPort,
                cqlshWorkPath,
                cqlshBinPath,
                nodetoolWorkPath,
                nodetoolBinPath,
                nodetoolPort,
                binDirPath,
                dataDirPath,

            )
        }
    }
}

