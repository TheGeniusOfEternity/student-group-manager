package handlers

import State
import kotlin.collections.ArrayList
import commands.InsertCmd
import commands.UpdateCmd
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * IOHandler of application input/output:
 * - Reads data & script files
 * - Reads user input on commands [InsertCmd] & [UpdateCmd]
 * - Writes output for files and console (mostly)
 * @property responsesThreads - storage for all command responses in string representation
 */

object IOHandler {
    val responsesThreads: ConcurrentHashMap<String, ArrayList<Pair<String, String>?>> = ConcurrentHashMap()

    /**
     * Loads auth credentials for connection to broker & database from .env file
     */
    fun loadCredentials() {
        val envFile = File(".env")
        if (envFile.exists()) {
            envFile.readLines()
                .forEach { line ->
                    this printInfoLn line
                    val trimmedLine = line.trim()
                    if (trimmedLine.isNotEmpty() && trimmedLine.contains("=")) {
                        val parts = trimmedLine.split("=", limit = 2)
                        if (parts.size == 2) {
                            State.credentials[parts[0]] = parts[1]
                        } else {
                            this printInfoLn "Invalid line format (missing value): $line"
                        }
                    } else {
                        this printInfoLn "Skipping invalid or empty line: $line"
                    }
                }
            this printInfoLn ".env file successfully loaded"
        } else {
            this printInfoLn ".env file not found"
        }
    }


    infix fun printInfoLn(message: String?): Unit = println(message)
}