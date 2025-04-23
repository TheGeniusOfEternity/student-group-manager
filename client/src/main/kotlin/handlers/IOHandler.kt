package handlers

import GroupData
import State
import annotations.Nested
import collection.StudyGroup
import parsers.InputParser
import parsers.OutputParser
import validators.GroupDataValidator
import validators.PropertyValidator
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.reflect.KClass
import receiver.Receiver
import java.util.concurrent.TimeUnit

/**
 * IOHandler of application input/output:
 * - Reads data & script files
 * - Reads user input on commands [InsertCmd] & [UpdateCmd]
 * - Writes output for files and console (mostly)
 */

object IOHandler {
    /**
     * Main function of i/o handle,
     * Works only if [State.isRunning] is true
     */
    fun handle() {
        if (!State.connectedToServer ) {
            ConnectionHandler.handleConnectionFail()
        }
        if (State.tasks == 1) {
            IOHandler printInfo "& "
            InputParser.parseCommand()
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