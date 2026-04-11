package ru.dude.cass.check.casstool

import java.io.File


/**
 * @author Vladimir X
 * Date: 14.02.2026
 */
class ShellRun {


    /**
     * Запускает переданную комманду
     */

    fun runCommand(workDir: String, command: String) = runCommand(workDir, command.split(" "))

    fun runCommand(workDir: String, commandParts: List<String>): String {
        val process = ProcessBuilder(commandParts)
            .directory(File(workDir))
            .redirectErrorStream(true) // Combine standard error and standard output
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor() // Wait for the process to complete

        if (process.exitValue() != 0) {
            throw RuntimeException("Command failed with exit code ${process.exitValue()}: $output")
        }

        return output
    }

    /**
     * Запуск через подготовленный bash script
     * Скрипт должен записать в out запущенный pid процесса
     */
    fun runAsyncFormShFile(workDir: String, command: String): String {
        val process = ProcessBuilder(listOf("bash", command))
            .directory(File(workDir))
            .redirectErrorStream(true)
            .start()


        val output = process.inputStream.bufferedReader().use { it.readText() }

        val pid = Regex("""\d+""").find(output)?.value.orEmpty()
        println("PID: $pid")

        process.waitFor() // Wait for the process to complete

        if (process.exitValue() != 0) {
            throw RuntimeException("Command failed with exit code ${process.exitValue()}: $output")
        }

        return pid

    }


}
