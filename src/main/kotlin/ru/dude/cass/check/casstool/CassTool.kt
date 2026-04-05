package ru.dude.cass.check.casstool

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


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
        logger.info("${config.cassVersion} collecting context info")

        runInfo = cassInfo()

    }

    private fun runCassShell() {

        when (config.cassVersion) {
            CassVersion.CASS_4,
            CassVersion.CASS_5 -> shellRunner.runCommand(config.cassPath, "${config.cassBinFile} -p $pidFile")

            CassVersion.SCYLLA -> shellRunner.runAsyncFormShFile(
                config.cassPath,
                config.cassBinFile
            )
        }

    }


    fun cassIsRunning(printDot: Boolean): Boolean {
        return try {

            val stat = cassStatus()
            return stat.contains("UN")
        } catch (_: RuntimeException) {
            false
        } finally {
            if (printDot) {
                print(".")
            }
        }
    }


    fun cassStatus() = shellRunner.runCommand(config.cassPath, "${config.cassNodetoolFile} status")

    fun cassInfo(): CassandraRunInfo {

        if (config.cassVersion == CassVersion.SCYLLA) {
            return CassandraRunInfo.SCYLLA

        }

        val pid = Files.readString(Path.of(config.cassPath + "/$pidFile"))
        val psInfo = shellRunner.runCommand(".", "ps -o command -p $pid").split("\n").getOrNull(1)


        if (psInfo == null) {
            logger.info("Cassandra daemon not detected. Info can't be collected")
            return CassandraRunInfo.UNDEFINED
        }

        val javaVersion = Regex("""^.+java\s""").find(psInfo)?.value?.let { javaExecPath ->

            val javaVesionInfo = shellRunner.runCommand(".", javaExecPath + "-version").split("\n").firstOrNull() ?: ""
            val versionStr = Regex(""""(\d+|\.)+"""").find(javaVesionInfo)?.value ?: "\"\""
            versionStr.drop(1).dropLast(1)

        } ?: "Unknown"

        val tablePhoneInfo = shellRunner.runCommand("${config.cassPath}/bin",
            listOf("cqlsh", "127.0.0.1", config.cassNativePort.toString(), "-e", "DESC store.phone")
        )

        val ssTablesPhoneDir = File(config.keyspaceStoreDirectory).listFiles().first{it.name.startsWith("phone-")}
        val ssTablesPhoneFormat =  ssTablesPhoneDir.listFiles().first { it.name.endsWith("-Data.db") }.let { it.name }.let { it.substringBeforeLast('-').substringAfterLast('-') }

        return CassandraRunInfo(javaVersion , CassandraGC.byPsInfo(psInfo), config.cassVersion.name, ssTablesPhoneFormat)
    }

    fun cassStop() {

        if (cassIsRunning(false)) {
            logger.info("${config.cassVersion} stopping...")

            val pid = Files.readString(Path.of(config.cassPath, pidFile))
            shellRunner.runCommand(config.cassPath, "kill $pid")

            logger.info("${config.cassVersion} stopping awaiting...")

            while (cassIsRunning(true)) {
                Thread.sleep(500)
            }
            println("done")

            logger.info("${config.cassVersion} stopped")
        } else {
            logger.info("${config.cassVersion} is not running")
        }


    }


    fun createKeyspace() {

        logger.info("${config.cassVersion} drop keyspace and data files")

        val dropCql = "drop KEYSPACE if exists store;"
        shellRunner.runCommand("${config.cassPath}/bin",
            listOf("cqlsh", "127.0.0.1", config.cassNativePort.toString(), "-e", dropCql)
        )


        // удаляю старые дирректории чтобы не мешались
        File(config.keyspaceStoreDirectory).deleteRecursively()

        logger.info("${config.cassVersion} create-keyspace begin...")

        // запускаю создание схемы
        shellRunner.runCommand(".", "cqlsh 127.0.0.1 ${config.cassNativePort} -f ${config.createKeyspaceScriptPath}")

        logger.info("${config.cassVersion} create-keyspace complete")

    }

    fun flushAndCompact(tableName: String) {

        logger.info("${config.cassVersion} flush")
        shellRunner.runCommand(config.cassPath, "${config.cassNodetoolFile} flush")

        logger.info("${config.cassVersion} compact $tableName")
        shellRunner.runCommand(config.cassPath, "${config.cassNodetoolFile} compact ${config.cassKeyspaceName} $tableName")

    }

    fun tableOnDiscSizeMb(tableName: String): Long {

        val totalSize = File("${config.cassPath}/data/data/store")
            .listFiles { _, name -> name.startsWith("$tableName-") }
            .sumOf { f -> dirSize(f) }

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
