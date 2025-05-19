package handlers

import State
import collection.CollectionInfo
import collection.StudyGroup
import parsers.InputParser
import validators.GroupDataValidator
import java.io.FileReader
import java.io.IOException
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
     * @param filename - Path to the file
     * @param lastLine - Index of last read line of the file
     *
     * @return [ArrayList] of [StudyGroup] if data file is being read, or null in script file case
     */
    fun handleFileInput(filename: String, lastLine: Int?): ArrayList<StudyGroup?>? {
        var response: ArrayList<StudyGroup?>? = null
        try {
            CollectionInfo.addOpenedFile(Pair(filename, lastLine))
            val fileReader = FileReader(filename)
            if (CollectionInfo.getOpenedFiles().lastElement().first.contains("data/")) {
                val groupDataValidator = GroupDataValidator()
                val groupsData = InputParser.parse(fileReader)
                response = groupsData.map {groupData ->
                    groupDataValidator.validateData(groupData)
                }.toCollection(ArrayList())
            }
        } catch (e: IOException) {
            IOHandler printInfoLn "input error: file $filename not found"
        }
        CollectionInfo.removeOpenedFile()
        return response
    }

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