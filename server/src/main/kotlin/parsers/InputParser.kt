package parsers

import GroupData
import Property
import dto.CommandParam
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
}