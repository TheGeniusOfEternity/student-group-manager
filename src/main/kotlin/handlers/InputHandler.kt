package handlers

import GroupData
import State
import annotations.Nested
import collection.CollectionInfo
import collection.StudyGroup
import parsers.InputParser
import validators.GroupDataValidator
import validators.PropertyValidator
import java.io.FileReader
import java.io.IOException
import kotlin.reflect.KClass

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

    /**
     * Handle user's input on Insert command and creates GroupData
     *
     * @param data A [GroupData] object, contains only ("id", id)
     * @param option class's name to get its properties
     */
    fun handleUser(data: GroupData, option: String): StudyGroup? {
        val propertyValidator = PropertyValidator()
        val groupDataValidator = GroupDataValidator()
        val properties = propertyValidator.getProperties(className = option)
        for ((property) in properties) {
            if (property.annotations.contains(Nested())) {
                if (property.name == "groupAdmin") {
                    var input: String
                    do {
                        print("${property.name.replaceFirstChar { it.uppercase() }} (Y/n): ")
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") handleUser(data, property.returnType.toString().split("?")[0])
                } else {
                    handleUser(data, property.returnType.toString().split("?")[0])
                }
            }
            else {
                if (property.name == "id" || property.name == "creationDate") continue
                var input: String
                do {
                    print("${property.name.replaceFirstChar { it.uppercase() }}: ")
                    if ((property.returnType.classifier as? KClass<*>)?.java?.isEnum == true) {
                        val enums = (property.returnType.classifier as? KClass<*>)?.java?.enumConstants?.joinToString(separator = ", ")
                        print("($enums) ")
                    }
                    input = readln()

                } while (!propertyValidator.validateData(Pair(property.name, input)))
                data.add(Pair(property.name, input))
            }
        }
        if (option == "collection.StudyGroup") {
            return groupDataValidator.validateData(data)
        }
        return null
    }
}