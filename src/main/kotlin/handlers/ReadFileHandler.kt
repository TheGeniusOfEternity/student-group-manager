package handlers

import GroupData
import Property
import collection.StudyGroup
import validators.GroupDataValidator
import java.io.FileReader
import java.io.IOException
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

class ReadFileHandler : Handler<String> {
    /**
     * Read StudyGroups from file and create new [GroupData]
     *
     * @param data path to file needed to read
     * @param option
     *
     * @return new [GroupData] or null if nothing was found
     */
    override fun handle(data: String, option: String): ArrayList<StudyGroup?>? {
        try {
            val fileReader = FileReader(data)
            val groupDataValidator = GroupDataValidator()
            val groups = ArrayList<StudyGroup?>()
            var index: Int = fileReader.read()
            val currentGroupData: GroupData = GroupData()
            var currentField = ""
            var className = "collection.StudyGroup"
            var propPos = 10
            while (index != -1) {
                when (val c: Char = index.toChar()) {
                    ';' -> {
                        val propName = groupDataValidator.getPropertyNameForValidation(propPos, className)
                        if (propName != null) {
                            if (propName.name == "coordinates" || propName.name == "groupAdmin") {
                                className = propName.returnType.toString().split("?")[0]
                                propPos++
                                continue
                            } else if (className != "collection.StudyGroup") {
                                currentGroupData.add(Property(propName.name, currentField))
                                currentField = ""
                                propPos++
                            } else {
                                currentGroupData.add(Property(propName.name, currentField))
                                currentField = ""
                                propPos += 10
                            }
                        } else {
                            className = "collection.StudyGroup"
                            propPos = ceil(propPos.toDouble() / 10).toInt() * 10
                            continue
                        }
                    }
                    '\n' -> {
                        val propName = groupDataValidator.getPropertyNameForValidation(propPos, className)
                        if (propName != null) {
                            currentGroupData.add(Property(propName.name, currentField))
                            currentField = ""
                            groups.add(groupDataValidator.validateData(currentGroupData))
                            currentGroupData.clear()
                            propPos = 10
                        }
                    }
                    '"' -> {
                        currentField += ""
                    }
                    else -> {
                        currentField += c
                    }
                }
                index = fileReader.read()
            }
            return groups
        } catch (e: IOException) {
            println("read $data error: no such file found")
        }
        return null
    }
}