package commands

import GroupData
import Property
import State
import receiver.Receiver
import validators.PropertyValidator
import collection.StudyGroup
import handlers.IOHandler

/**
 * Update [StudyGroup] by its id, works similar to [InsertCmd]
 */
class UpdateCmd : Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            State.source = InputSource.CONSOLE
            val propertyValidator = PropertyValidator()
            val newGroupData = GroupData()
            if (propertyValidator.validateData(Property("id", (args[0] as String)))) {
                if (Receiver.getStudyGroup((args[0] as String).toLong()) != null) {
                    newGroupData.add(Property("id", (args[0] as String)))
                    val newGroup = IOHandler.handleUserInput(newGroupData, "collection.StudyGroup")
                    if (newGroup != null) {
                        Receiver.addStudyGroup((args[0] as String).toLong(), newGroup)
                        IOHandler printInfoLn "Successfully updated new group, type 'show' to see all groups"
                    } else {
                        IOHandler printInfoLn "update error: group data can't be validated"
                    }
                } else {
                    var input: String
                    do {
                        IOHandler printInfo "update error: group #${(args[0] as String)} not found, should insert? (Y/n): "
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") {
                        InsertCmd().execute(args)
                    } else {
                        IOHandler printInfoLn "update rejected"
                    }
                }
            }
        } else {
            IOHandler printInfoLn "update: invalid count of arguments"
        }
    }

    override fun describe(): String {
        return "update <key> {element} - updates element from collection with provided id"
    }
}