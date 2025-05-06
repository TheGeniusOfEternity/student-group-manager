package parsers

import core.GroupData
import core.Property
import core.State
import invoker.Invoker
import java.io.FileReader

/**
 * Parser for file input
 */
object InputParser: Parser<FileReader> {
    /**
     * @param data [FileReader] of reading file
     * @return ArrayList of [GroupData]
     */
    override fun parse(data: FileReader): ArrayList<GroupData> {
        val groupsData = ArrayList<GroupData>()
        var currentGroupData: GroupData = GroupData()
        val properties = arrayOf("id", "name", "x", "y", "studentsCount", "transferredStudents", "averageMark", "formOfEducation", "name", "birthday", "nationality")
        var currentField = ""
        var propName: String
        var propPos = 0
        var index: Int = data.read()
        while (index != -1) {
            when (val c: Char = index.toChar()) {
                ';' -> {
                    propName = properties[propPos]
                    currentGroupData.add(Property(propName, currentField))
                    currentField = ""
                    propPos++
                }
                '\n' -> {
                    if (propPos == 10) {
                        propName = "nationality"
                        currentGroupData.add(Property(propName, currentField))
                    }
                    groupsData.add(currentGroupData)
                    currentGroupData = GroupData()
                    currentField = ""
                    propPos = 0
                }
                '\'' -> {
                    currentField += ""
                }
                else -> {
                    currentField += c
                }
            }
            index = data.read()
        }
        return groupsData
    }

    /**
     * Parses script files
     *
     * @param data - [FileReader]
     * @param fileName - Name of reading file
     */
    fun parseScript(data: FileReader, fileName: String) {
        var index: Int = data.read()
        var currLine = ""
        val prevLines = State.getFileByName(fileName)?.second ?: -1
        var linesCount = 0
        while (index != -1) {
            when (val c: Char = index.toChar()) {
                '\n' -> {
                    if (currLine.isNotEmpty() && linesCount > prevLines) {
                        val query: List<String> = currLine.trim().split(" ")
                        val commandName = query.first()
                        Invoker.run(commandName, query.drop(1))
                        State.updateOpenedFile(fileName, linesCount)
                    }
                    currLine = ""
                    linesCount++
                }
                else -> {
                    currLine += c
                }
            }
            index = data.read()
        }
    }

    /**
     * Reads commands and invoke them to execute
     */
    fun parseCommand() {
        val commandName: String
        val input = readlnOrNull()
        if (!input.isNullOrEmpty()) {
            val query: List<String> = input.trim().split(" ")
            commandName = query.first()
            Invoker.run(commandName, query.drop(1))
        }
    }
}