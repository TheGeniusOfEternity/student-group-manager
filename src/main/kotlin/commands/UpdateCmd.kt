package commands

import GroupData
import Property
import State
import receiver.Receiver
import validators.PropertyValidator
import collection.StudyGroup
import handlers.InputHandler
import parsers.InputParser

/**
 * Update [StudyGroup] by its id, works similar to [InsertCmd]
 */
class UpdateCmd : Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            State.source = InputSource.CONSOLE
            val propertyValidator = PropertyValidator()
            val newGroupData = GroupData()
            if (propertyValidator.validateData(Property("id", args[0]))) {
                if (Receiver.getStudyGroup(args[0].toLong()) != null) {
                    newGroupData.add(Property("id", args[0]))
                    val newGroup = InputHandler.handleUser(newGroupData, "collection.StudyGroup")
                    if (newGroup != null) {
                        Receiver.addStudyGroup(args[0].toLong(), newGroup)
                        println("Successfully updated new group, type 'show' to see all groups")
                    } else {
                        println("update error: group data can't be validated")
                    }
                } else {
                    var input: String
                    do {
                        print("update error: group #${args[0]} not found, should insert? (Y/n): ")
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") {
                        InsertCmd().execute(args)
                    } else {
                        println("update rejected")
                    }
                }
            }
        } else {
            println("update: invalid count of arguments")
        }
    }

    override fun describe() {
        println("update <key> {element} - updates element from collection with provided id")
    }
}