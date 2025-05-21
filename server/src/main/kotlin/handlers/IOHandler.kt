package handlers

import State
import java.util.*
import kotlin.collections.ArrayList
import commands.InsertCmd
import commands.UpdateCmd
import java.io.File

/**
 * IOHandler of application input/output:
 * - Reads data & script files
 * - Reads user input on commands [InsertCmd] & [UpdateCmd]
 * - Writes output for files and console (mostly)
 * @property responsesThreads - storage for all command responses in string representation
 */

object IOHandler {
    val responsesThreads: HashMap<String, ArrayList<String?>> = HashMap()

    /**
     * Loads auth credentials for connection to broker & database from .env file
     */
    fun loadCredentials() {
        val envFile = File(".env")
        if (envFile.exists()) {
            envFile.readLines()
                .forEach { line ->
                    State.credentials[line.split("=")[0]] = line.split("=")[1]
                }
            this printInfoLn ".env file successfully loaded"
        } else this printInfoLn ".env file not found"
    }

    infix fun printInfoLn(message: String?): Unit = println(message)
}