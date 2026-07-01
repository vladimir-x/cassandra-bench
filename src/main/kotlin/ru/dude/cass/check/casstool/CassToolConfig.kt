package ru.dude.cass.check.casstool

import ru.dude.cass.check.casstool.CassTool.CassVersion
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
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

    val cassDataPath = when (cassVersion) {
        CassVersion.CASS_4 -> properties.getProperty("casstool.cassandra4.data.path")
        CassVersion.CASS_5 -> properties.getProperty("casstool.cass5.data.path")
        CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.data.path")
    }

    val createKeyspaceScriptPath = when (cassVersion) {
        CassVersion.CASS_4 -> properties.getProperty("casstool.cass4.create-keyspace-script")
        CassVersion.CASS_5 -> properties.getProperty("casstool.cass5.create-keyspace-script")
        CassVersion.SCYLLA -> properties.getProperty("casstool.scylla.create-keyspace-script")
    }

    private val cassEnvFile = "$cassPath/conf/cassandra-env.sh"
    private val cassYamlFile = when (cassVersion) {
        CassVersion.CASS_4,
        CassVersion.CASS_5 -> "$cassPath/conf/cassandra.yaml"

        CassVersion.SCYLLA -> "$cassPath/conf/scylla.yaml"
    }

    private val cassJvmServerOptsFile = "$cassPath/conf/jvm-server.options"
    private val cassJvmServerOptsCopyFile = "$cassPath/conf/jvm-server-cp.options"


    private val scyllaRunAsDaemonScriptSh = properties.getProperty("casstool.scylla.run-as-daemon-script")


    val cassBinFile = when (cassVersion) {
        CassVersion.CASS_4,
        CassVersion.CASS_5 -> "$cassPath/bin/cassandra"

        CassVersion.SCYLLA -> scyllaRunAsDaemonScriptSh
    }

    val selectedJavaHome = when (properties.getProperty("casstool.java.version")) {
        "java11" -> properties.getProperty("casstool.java11.home")
        "java17" -> properties.getProperty("casstool.java17.home")
        else -> throw Exception("Unexpected Java version")
    }


    val cassNodetoolFile = "$cassPath/bin/nodetool"
    // val cqlshFile = "$cassPath/bin/cqlsh"

    val keyspaceStoreDirectory = "${cassDataPath}/store"

    //для подключения через nodetool
    val cassJmxPort = intFromFirstLine(readAllLines(cassEnvFile), "JMX_PORT=", 7199)

    //для подключения через cqlsh
    val cassNativePort = intFromFirstLine(readAllLines(cassYamlFile), "native_transport_port:", 9042)

    val cassKeyspaceName = properties.getProperty("spring.cassandra.keyspace-name")

    //для подключения через cqlsh
    val cassHost = properties.getProperty("spring.cassandra.contact-points").split(',').map(String::trim).first().split(":")[0]
    val cassPort = properties.getProperty("spring.cassandra.contact-points").split(',').map(String::trim).first().split(":")[1]

    fun updateCassJvmServerOptions() {

        val optXmx = properties.getProperty("casstool.java.xmx", "-Xmx2g").trim()
        val optXms = properties.getProperty("casstool.java.xms", "-Xms2g").trim()
        val propGc = properties.getProperty("casstool.java.gc", "G1")
        val optGC = when {
            propGc.equals("CMS", ignoreCase = true) -> "-XX:+UseConcMarkSweepGC"
            propGc.equals("G1", ignoreCase = true) -> "-XX:+UseG1GC"
            propGc.equals("ZGC", ignoreCase = true) -> "-XX:+UseZGC"
            else -> "-XX:+UseG1GC"
        }


        val optsFile = File(cassJvmServerOptsFile)
        val copyFile = File(cassJvmServerOptsCopyFile)

        // 0 Делаем копию настроек, если её ещё нет
        if (!copyFile.exists()) {

            Files.copy(optsFile.toPath(), copyFile.toPath())
        }

        // вычитываем настройки из копии
        val optLines = Files.readAllLines(copyFile.toPath(), Charsets.UTF_8)

        // меняем настройки
        optLines.forEachIndexed { index, str ->
            if (str.startsWith("### GC HERE ###")) {
                optLines[index + 1] = optGC
            }

            if (str.startsWith("### HEAP HERE ###")) {
                optLines[index + 1] = optXmx
                optLines[index + 2] = optXms
            }
        }

        // подкладываем настройки
        Files.writeString(optsFile.toPath(), optLines.joinToString("\n"), Charsets.UTF_8, StandardOpenOption.WRITE)


    }

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

    private fun intFromFirstLine(lines: List<String>, containsPart: String, defaultPort: Int): Int {
        return lines.firstOrNull { it.contains(containsPart) }?.let {
            REGEX_INT.find(it)!!.value.toInt()
        } ?: -defaultPort

    }


}
