package handlers

import State
import collection.CollectionInfo
import collection.StudyGroup
import parsers.InputParser
import validators.GroupDataValidator
import java.io.FileReader
import java.io.IOException

/**
 * Handler of application input: reading data files & script files
 */

object InputHandler : Handler<String, Int?> {
    /**
     * @param data - Path to the file
     * @param option - Index of last read line of the file
     *
     * @return [ArrayList] of [StudyGroup] if data file is being read, or null in script file case
     */
    override fun handle(data: String, option: Int?): ArrayList<StudyGroup?>? {
        var response: ArrayList<StudyGroup?>? = null
        try {
            CollectionInfo.addOpenedFile(Pair(data, option))
            val fileReader = FileReader(data)
            if (CollectionInfo.getOpenedFiles().lastElement().first.contains("data/")) {
                val groupDataValidator = GroupDataValidator()
                val groupsData = InputParser.parse(fileReader)
                response = groupsData.map {groupData ->
                    groupDataValidator.validateData(groupData)
                }.toCollection(ArrayList())
            } else {
                InputParser.parseScript(fileReader, data)
            }
            CollectionInfo.removeOpenedFile()
        } catch (e: IOException) {
            println("input error: file $data not found")
        }
        State.source = InputSource.CONSOLE
        return response
    }
}