package handlers

import core.GroupData
import core.State
import annotations.Nested
import collection.StudyGroup
import parsers.InputParser
import validators.GroupDataValidator
import validators.PropertyValidator
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.reflect.KClass
import java.io.FileReader

/**
 * IOHandler of application input/output:
 * - Reads data & script files
 * - Writes output for files and console (mostly)
 * @property responsesThreads - storage for all command responses in string representation
 */

object IOHandler {
    val responsesThreads: ArrayList<String?> = ArrayList()
    /**
     * Main function of i/o handle,
     * Works only if [State.isRunning] is true
     */
    fun handle() {
        if (responsesThreads.isNotEmpty()) {
            responsesThreads.forEach{response ->
                IOHandler printInfoLn response
            }
            responsesThreads.clear()
        }
        if (!State.connectedToServer) ConnectionHandler.handleConnectionFail()
        if (State.tasks == 1) {
            IOHandler printInfo "& "
            InputParser.parseCommand()
        }
    }

    /**
     * Get IPv4 address of server from user
     */
    fun getServerAddress() {
        while (State.host == null) {
            IOHandler printInfoLn "Specify server ipv4 address:"
            IOHandler printInfo "& "
            val input = readln()
            if (isValidIPv4(input)) State.host = input
            else printInfoLn("Incorrect IPv4 address.")
        }
    }

    /**
     * Checks if provided IP is valid for IPv4
     * @return true, if ip is valid, false otherwise
     */
    private fun isValidIPv4(ip: String): Boolean {
        val parts = ip.split(".")
        if (ip.trim() == "localhost") return true
        if (parts.size != 4) return false
        for (part in parts) {
            if (part.isEmpty() || part.length > 3) return false
            if (part.length > 1 && part.startsWith('0')) return false
            val num = part.toIntOrNull() ?: return false
            if (num !in 0..255) return false
        }
        return true
    }

    fun getAuthCredentials() {
        State.credentials["TEMP_USERNAME"] = ""
        State.credentials["TEMP_PASSWORD"] = ""
        while (State.credentials["TEMP_USERNAME"]!!.isEmpty()) {
            IOHandler printInfoLn "Username:"
            IOHandler printInfo "& "
            State.credentials["TEMP_USERNAME"] = readln().trim()
        }
        while (State.credentials["TEMP_PASSWORD"]!!.isEmpty()) {
            IOHandler printInfoLn "Password:"
            IOHandler printInfo "& "
            State.credentials["TEMP_PASSWORD"] = readln().trim()
        }
    }

    /**
     * Handle user's input on Insert command and creates GroupData
     *
     * @param data A [GroupData] object, contains only ("id", id)
     * @param classname class's name to get its properties
     *
     * @return New [StudyGroup] or null
     */
    fun handleUserInput(data: GroupData, classname: String): StudyGroup? {
        val propertyValidator = PropertyValidator()
        val groupDataValidator = GroupDataValidator()
        val properties = propertyValidator.getProperties(classname)
        for ((property) in properties) {
            if (property.annotations.contains(Nested())) {
                if (property.name == "groupAdmin") {
                    var input: String
                    do {
                        IOHandler printInfo "${property.name.replaceFirstChar { it.uppercase() }} (Y/n): "
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") handleUserInput(data, property.returnType.toString().split("?")[0])
                } else {
                    handleUserInput(data, property.returnType.toString().split("?")[0])
                }
            }
            else {
                if (property.name == "id" || property.name == "creationDate") continue
                var input: String
                do {
                    IOHandler printInfo "${property.name.replaceFirstChar { it.uppercase() }}: "
                    if ((property.returnType.classifier as? KClass<*>)?.java?.isEnum == true) {
                        val enums = (property.returnType.classifier as? KClass<*>)?.java?.enumConstants?.joinToString(separator = ", ")
                        IOHandler printInfo "($enums) "
                    }
                    input = readln()

                } while (!propertyValidator.validateData(Pair(property.name, input)))
                data.add(Pair(property.name, input))
            }
        }
        if (classname == "collection.StudyGroup") {
            return groupDataValidator.validateData(data)
        }
        return null
    }
    /**
     * @param filename - Path to the file
     * @param lastLine - Index of last read line of the file
     *
     * @return [ArrayList] of [StudyGroup] if data file is being read, or null in script file case
     */
    fun handleFileInput(filename: String, lastLine: Int?): ArrayList<StudyGroup?>? {
        var response: ArrayList<StudyGroup?>? = null
        try {
            State.addOpenedFile(Pair(filename, lastLine))
            val fileReader = FileReader(filename)
            if (State.getOpenedFiles().lastElement().first.contains("data/")) {
                val groupDataValidator = GroupDataValidator()
                val groupsData = InputParser.parse(fileReader)
                response = groupsData.map {groupData ->
                    groupDataValidator.validateData(groupData)
                }.toCollection(ArrayList())
            } else {
                InputParser.parseScript(fileReader, filename)
            }
            State.removeOpenedFile()
        } catch (e: IOException) {
            IOHandler printInfoLn "input error: file $filename not found"
        }
        return response
    }

    /**
     * Loads auth credentials for connection to broker from .env file
     */
    fun loadCredentials() {
        val envFile = File(".env")
        if (envFile.exists()) {
            envFile.readLines()
                .forEach { line ->
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


    infix fun printInfo(message: String?): Unit = print(message)
    infix fun printInfoLn(message: String?): Unit = println(message)
}