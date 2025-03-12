package handlers

import GroupData
import collection.CollectionInfo
import collection.StudyGroup
import parsers.InputParser
import validators.GroupDataValidator
import java.io.FileReader
import java.io.IOException
import kotlin.collections.ArrayList

class ReadDataFileHandler : Handler<String, Int?> {
    /**
     * Read StudyGroups from file and create new [GroupData]
     *
     * @param data Path to file needed to read
     * @param option Current index of reading file
     *
     * @return new [GroupData] or null if nothing was found
     */
    override fun handle(data: String, option: Int?): ArrayList<StudyGroup?>? {
        try {
            val fileReader = FileReader(data)
            CollectionInfo.setOpenedFilename(Pair(data, option))
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
            State.source = InputSource.CONSOLE
            fileReader.close()
            CollectionInfo.removeOpenedFileName()
            return groups
        } catch (e: IOException) {
            println("read $data error: no such file found")
            return null
        }
    }
}