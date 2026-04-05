package ru.dude.cass.check.casstool

import ru.dude.cass.check.casstool.CassTool.CassVersion
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.exists


/**
 * @author Vladimir X
 * Date: 14.02.2026
 */
class CassToolConfig(val propertyFilePath: String) {


    private companion object {

        val REGEX_INT = Regex("\\d+")
    }

    private val properties = openFile()

    val cassVersion = CassVersion.valueOf(properties.getProperty("casstool.cassandra.enable-version"))

    val startEnable = properties.getProperty("casstool.start.enable").toBoolean()

    val forceRestart = properties.getProperty("casstool.force-restart.enable").toBoolean()

    val stopEnable = properties.getProperty("casstool.stop.enable").toBoolean()

    val createKeyspaceEnable = properties.getProperty("casstool.create-keyspace.enable").toBoolean()

    val cassPath = when (cassVersion) {
        CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.path")
        CassVersion.CASS_5 -> properties.getProperty("casstool.cassandra5.path")
        CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.path")
    }

    val createKeyspaceScriptPath = when (cassVersion) {
        CassVersion.CASS_4 -> properties.getProperty("casstool.cass4.create-keyspace-script")
        CassVersion.CASS_5 -> properties.getProperty("casstool.cass5.create-keyspace-script")
        CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.create-keyspace-script")
    }

    private val cassEnvFile = "$cassPath/conf/cassandra-env.sh"
    private val cassYamlFile = when (cassVersion) {
        CassVersion.CASS_4,
        CassVersion.CASS_5 ->  "$cassPath/conf/cassandra.yaml"
        CassVersion.SCYLLA ->  "$cassPath/conf/scylla.yaml"
    }





    val cassBinFile = when (cassVersion) {
        CassVersion.CASS_4,
        CassVersion.CASS_5 -> "$cassPath/bin/cassandra"

        CassVersion.SCYLLA -> "$cassPath/scylla_run_as_daemon.sh"
    }


    val cassNodetoolFile = "$cassPath/bin/nodetool"

    val cqlshFile = "$cassPath/bin/cqlsh"

    val keyspaceStoreDirectory =  "$cassPath/data/data/store"

    //для подключения через nodetool
    val cassJmxPort = intFromFirstLine(readAllLines(cassEnvFile), "JMX_PORT=")

    //для подключения через cqlsh
    val cassNativePort = intFromFirstLine(readAllLines(cassYamlFile), "native_transport_port:")

    val cassKeyspaceName = properties.getProperty("spring.cassandra.keyspace-name")

    private fun readAllLines(path: String): List<String> {
        return if (Path.of(path).exists()) {
            Files.readAllLines(Path.of(path))
        } else {
            emptyList()
        }
    }


    private fun openFile(): Properties {

        if (propertyFilePath.startsWith("classpath")) {
            val classpathName = propertyFilePath.substring("classpath:".length)
            javaClass.classLoader.getResourceAsStream(classpathName).use { s ->
                return Properties().also { it.load(s) }
            }
        } else {
            FileInputStream(propertyFilePath).use { s ->
                return Properties().also { it.load(s) }
            }
        }
    }

    private fun intFromFirstLine(lines: List<String>, containsPart: String): Int {
        return lines.firstOrNull { it.contains(containsPart) }?.let {
            REGEX_INT.find(it)!!.value.toInt()
        } ?: -999

    }


}
