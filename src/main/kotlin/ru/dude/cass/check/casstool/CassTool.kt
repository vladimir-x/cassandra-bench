package ru.dude.cass.check.casstool

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists


/**
 * @author Vladimir X
 * Date: 13.02.2026
 */
class CassTool(propertyFilePath: String) {


    enum class CassVersion {
        CASS_4, CASS_5, SCYLLA
    }

    companion object {
        const val pidFile = "cass_pid"

        private val logger = LoggerFactory.getLogger(CassTool::class.java)

        val INSTANCE = CassTool("classpath:application.properties")
    }

    val config = CassToolConfig(propertyFilePath)

    private val shellRunner = ShellRun()

    /**
     * Информация о запущенном инстансе кассандры
     */
    var runInfo = CassandraRunInfo.UNDEFINED
        private set

    fun cassStart() {

        if (cassIsRunning(false)){
            if (config.forceRestart) {
                logger.info("${config.cassVersion} force restart... ")
                cassStop()
            } else {
                logger.info("${config.cassVersion} already started")
                return
            }
        }


        logger.info("${config.cassVersion} starting (bin: ${config.cassBinFile}, jmxPort: ${config.cassJmxPort}, native_transport_port: ${config.cassNativePort})...")

        runCassShell()

        logger.info("${config.cassVersion} node up waiting... ")

        while (!cassIsRunning(true)) {
            Thread.sleep(500)
        }
        logger.info("${config.cassVersion} start complete")
    }

    fun cassCollectInfo() {
        logger.info("${config.cassVersion} collecting context info...")

        runInfo = cassInfo()

        logger.info("Cass collected Info: {}", runInfo)

    }

    private fun runCassShell() {

        when (config.cassVersion) {
            CassVersion.CASS_4,
            CassVersion.CASS_5 -> {
                val env = mapOf("JAVA_HOME" to config.selectedJavaHome)

                config.updateCassJvmServerOptions()

                val outText =shellRunner.runCommand(".", "${config.cassBinFile} -p $pidFile", env)
                logger.debug(outText)
            }

             CassVersion.SCYLLA -> {

                 val (pid, outText) = shellRunner.runAsyncFormShFile(
                     ".",
                     config.cassBinFile
                 )
                 logger.debug(outText)
                 Files.writeString(Path.of(pidFile), pid,  StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
             }

        }

    }


    fun cassIsRunning(printDot: Boolean): Boolean {
        return try {

            val stat = cassStatus()
            if (stat.contains("UN")) {
                val cassVersion = cassServerVersion()
                logger.info("CASS srv_node_version: {}",cassVersion)
                true
            } else
                false
        } catch (_: RuntimeException) {
            false
        } finally {
            if (printDot) {
                print(".")
            }
        }
    }


    fun pidExisted(pid: String, printDot: Boolean): Boolean {
        return try {

            val command = shellRunner.runCommand(".", "ps -o command -p $pid").split("\n").getOrNull(1)
            return command != null

        } catch (_: RuntimeException) {
            false
        } finally {
            if (printDot) {
                print(".")
            }
        }
    }


    fun cassStatus() = shellRunner.runCommand(config.cassPath, "${config.cassNodetoolFile} status")

    fun cassServerVersion(): String {
        val versionText = cqlsh( "SHOW VERSION;")
        //val showVersion =
        //val res = shellRunner.runCommand("${config.cassPath}/bin",
        //    listOf( "cqlsh", config.cassHost, config.cassPort, "-e", showVersion)
        //)
        return versionText.split("|").getOrNull(1) ?: throw Exception("Cassandra version invalid")
    }

    fun cassInfo(): CassandraRunInfo {

        val serverVersion = cassServerVersion()

        if (config.cassVersion == CassVersion.SCYLLA) {
            return CassandraRunInfo.SCYLLA(serverVersion)

        }


        if (!Path.of(pidFile).exists()){
            logger.info("Pid file not exists: {}. Info unavailable", pidFile)
            return CassandraRunInfo.UNKNOWN(serverVersion)
        }


        val pid = Files.readString(Path.of(pidFile))

        val psInfo = if (pidExisted(pid, printDot = false)){
            shellRunner.runCommand(".", "ps -o command -p $pid").split("\n").getOrNull(1)
        } else {
            null
        }


        if (psInfo == null) {
            logger.info("Cassandra daemon not detected. Info can't be collected")
            return CassandraRunInfo.UNDEFINED
        }

        val javaVersion = Regex("""^.+java\s""").find(psInfo)?.value?.let { javaExecPath ->

            val javaVesionInfo = shellRunner.runCommand(".", javaExecPath + "-version").split("\n").firstOrNull() ?: ""
            val versionStr = Regex(""""(\d+|\.)+"""").find(javaVesionInfo)?.value ?: "\"\""
            versionStr.drop(1).dropLast(1)

        } ?: "Unknown"

        val xmx = Regex("""-Xmx\S+""").find(psInfo)?.value ?: "Undefined"

        val ssTablesPhoneDir = File(config.keyspaceStoreDirectory).listFiles().first{it.name.startsWith("phone-")}
        val ssTablesPhoneFormat =
            ssTablesPhoneDir.listFiles()
                .firstOrNull { it.name.endsWith("-Data.db") }?.let { it.name.substringBeforeLast('-').substringAfterLast('-') } ?: ""

        return CassandraRunInfo(javaVersion , CassandraGC.byPsInfo(psInfo), xmx, config.cassVersion.name, ssTablesPhoneFormat)
    }

    fun cassStop() {

        if (cassIsRunning(false)) {
            logger.info("${config.cassVersion} stopping...")

            val pidFile = Path.of(".", pidFile)

            val pid = Files.readString(pidFile).trim()

            if (pid.toIntOrNull() == null) {
                logger.info("${config.cassVersion} invalid PID [$pid]. Stop is aborted.")
                return
            }
            shellRunner.runCommand(".", "kill $pid")

            logger.info("${config.cassVersion} PID $pid stopping awaiting...")

            while (cassIsRunning(true)) {
                Thread.sleep(500)
            }

            while (pidExisted(pid, true)){
                Thread.sleep(500)
            }
            println("done")

            Files.deleteIfExists(pidFile)

            logger.info("${config.cassVersion} stopped")
        } else {
            logger.info("${config.cassVersion} is not running")
        }


    }


    fun createKeyspace() {

        logger.info("${config.cassVersion} drop keyspace and data files")

        val dropCql = "drop KEYSPACE if exists store;"
        shellRunner.runCommand("${config.cassPath}/bin",
            listOf("cqlsh", config.cassHost, config.cassPort, "-e", dropCql)
        )


        // удаляю старые дирректории чтобы не мешались
        File(config.keyspaceStoreDirectory).deleteRecursively()

        logger.info("${config.cassVersion} create-keyspace begin...")

        // запускаю создание схемы
        shellRunner.runCommand(".", "cqlsh ${config.cassHost} ${config.cassPort} -f ${config.createKeyspaceScriptPath}")

        logger.info("${config.cassVersion} create-keyspace complete")

    }

    fun flushAndCompact(tableName: String) {

        logger.info("${config.cassVersion} flush")
        try {
            nodetool("flush")
        } catch (e: Exception) {
            logger.error("Flush ${config.cassVersion} error: ${e.message}")
        }

        logger.info("${config.cassVersion} compact $tableName")
        try{
            nodetool("compact ${config.cassKeyspaceName} $tableName")
        } catch (e: Exception) {
            logger.error("Compact ${config.cassVersion} error: ${e.message}")
        }

    }

    fun nodetool(nodeToolCommand: String): String {
        return shellRunner.runCommand(
            config.cassPathConfig.nodetoolWorkPath,
            listOf(
                config.cassPathConfig.nodetoolBinPath,
                "-h",
                config.cassPathConfig.cassHost,
                "-p",
                config.cassPathConfig.nodetoolPort,
                nodeToolCommand
            )
        )
    }

    fun cqlsh(cqlScript: String): String {

        val env = mutableMapOf<String, String>()
        if (config.pythonBinPath.isNotBlank()) {
            env["USER_SPECIFIED_PYTHON"] = config.pythonBinPath
        }

        return shellRunner.runCommand(
            config.cassPathConfig.cqlshWorkPath,
            listOf(
                config.cassPathConfig.cqlshBinPath,
                config.cassPathConfig.cassHost,
                config.cassPathConfig.cassPort,
                "-e",
                cqlScript
            ),
            env
        )
    }


    fun tableOnDiscSizeMb(tableName: String): Long {

        val totalSize = File(config.keyspaceStoreDirectory)
            .listFiles { _, name -> name.startsWith("$tableName-") } ?.sumOf { f -> dirSize(f) } ?: return 0

        return totalSize / 1024 / 1024
    }


    private fun dirSize(dir: File): Long {
        return dir.listFiles()?.sumOf { f ->
            if (f.isDirectory) {
                dirSize(f)
            } else f.length()
        } ?: 0L

    }


}
