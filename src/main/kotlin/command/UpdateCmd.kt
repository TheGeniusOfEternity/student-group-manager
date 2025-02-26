package command

import receiver.Receiver
import receiver.Validator

class UpdateCmd : Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val validator = Validator()
            val newGroupData = ArrayList<String>()
            if (validator.valueIsValid(args[0], "id")) {
                if (Receiver.getStudyGroups()[args[0].toLong()] != null) {
                    newGroupData.add(args[0])
                    val newGroup = validator.validateInsertInput("collection.StudyGroup", newGroupData)
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