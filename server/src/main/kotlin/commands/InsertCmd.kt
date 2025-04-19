package commands
import GroupData
import State
import collection.StudyGroup
import handlers.IOHandler
import receiver.Receiver
import validators.PropertyValidator

/**
 * Starts insertion process: user manually step by step enters new [StudyGroup] data
 */
class InsertCmd : Command {
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            State.source = InputSource.CONSOLE
            val propertyValidator = PropertyValidator()
            val newGroupData = GroupData()
            if (propertyValidator.validateData(Pair("id", (args[0] as String)))) {
                if (Receiver.getStudyGroups()[(args[0] as String).toLong()] == null) {
                    newGroupData.add(Pair("id", (args[0] as String)))
                    val newGroup = IOHandler.handleUserInput(newGroupData, "collection.StudyGroup")
                    if (newGroup != null) {
                        Receiver.addStudyGroup((args[0] as String).toLong(), newGroup)
                        IOHandler printInfoLn "Successfully added new group, type 'show' to see all groups"
                    } else {
                        IOHandler printInfoLn "insert error: group data can't be validated"
                    }
                } else {
                    var input: String
                    do {
                        IOHandler printInfoLn "insert error: group #${(args[0] as String)} already exists, should update? (Y/n): "
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") {
                        UpdateCmd().execute(args)
                    } else {
                        IOHandler printInfoLn "insert rejected"
                    }
                }
            }
        } else {
            IOHandler printInfoLn "insert: invalid count of arguments"
        }
    }

    override fun describe(): String {
        return "insert <key> {element} - adds to collection element with <key> id"
    }
}