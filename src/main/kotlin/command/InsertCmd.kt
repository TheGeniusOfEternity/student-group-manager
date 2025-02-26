package command
import receiver.Receiver
import receiver.Validator
import kotlin.collections.ArrayList

class InsertCmd : Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val validator = Validator()
            val newGroupData = ArrayList<String>()
            if (validator.valueIsValid(args[0], "id")) {
                if (Receiver.getStudyGroups()[args[0].toLong()] == null) {
                    newGroupData.add(args[0])
                    val newGroup = validator.validateInsertInput("collection.StudyGroup", newGroupData)
                    if (newGroup != null) {
                        Receiver.addStudyGroup(args[0].toLong(), newGroup)
                        println("Successfully added new group, type 'show' to see all groups")
                    } else {
                        println("update error: group data can't be validated")
                    }
                } else {
                    var input: String
                    do {
                        print("insert error: group #${args[0]} already exists, should update? (Y/n): ")
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") {
                        UpdateCmd().execute(args)
                    } else {
                        println("insert rejected")
                    }
                }
            }
        } else {
            println("insert: invalid count of arguments")
        }
    }

    override fun describe() {
        println("insert <key> {element} - adds to collection element with <key> id")
    }
}