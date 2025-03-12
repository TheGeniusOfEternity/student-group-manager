package handlers

import GroupData
import Property
import collection.StudyGroup
import parsers.InputParser
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
            val inputParser = InputParser()
            val groups = ArrayList<StudyGroup?>()
            val groupsData = inputParser.parse(fileReader)
            groupsData.forEach{groupData ->
                val group = groupDataValidator.validateData(groupData)
                if (group != null) {
                    println("Group #${groupData[0].second} has been loaded")
                    groups.add(group)
                }
            }
            return groups
        } catch (e: IOException) {
            println("read $data error: no such file found")
            return null
        }
    }
}