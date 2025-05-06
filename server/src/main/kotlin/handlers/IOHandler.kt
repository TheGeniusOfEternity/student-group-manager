package handlers

import State
import collection.CollectionInfo
import collection.StudyGroup
import parsers.InputParser
import parsers.OutputParser
import validators.GroupDataValidator
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import commands.InsertCmd
import commands.UpdateCmd
import receiver.Receiver

/**
 * IOHandler of application input/output:
 * - Reads data & script files
 * - Reads user input on commands [InsertCmd] & [UpdateCmd]
 * - Writes output for files and console (mostly)
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
        State.source = InputSource.CONSOLE
        return response
    }

    /**
     * Writes output data to the file
     * @param data - [Receiver.stdGroupCollection] all study groups from collection
     * @param filename - name of file that data will be written in
     */
    fun handleFileOutput(data: TreeMap<Long, StudyGroup>, filename: String) {
        try {
            val writer = BufferedOutputStream(FileOutputStream(filename))
            val groupsData = OutputParser.generateGroupsData(data)
            val res = OutputParser.parse(groupsData)
            val bytes = res.toByteArray()
            writer.write(bytes)
            writer.flush()
        } catch (e: IOException) {
            IOHandler printInfo e.message
        }
    }

    infix fun printInfo(message: String?): Unit = print(message)
    infix fun printInfoLn(message: String?): Unit = println(message)
}