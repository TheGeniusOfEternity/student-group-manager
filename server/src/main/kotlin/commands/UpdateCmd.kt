package commands

import GroupData
import Property
import State
import receiver.Receiver
import validators.PropertyValidator
import collection.StudyGroup
import dto.CommandParam
import handlers.IOHandler

/**
 * Update [StudyGroup] by its id, works similar to [InsertCmd]
 */
class UpdateCmd : Command {
    override val paramTypeName = "StudyGroup"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            State.source = InputSource.CONSOLE
            val group = (args[0] as CommandParam.StudyGroupParam).value
                if (group != null) {
                    if (Receiver.getStudyGroup(group.getId()) != null) {
                        Receiver.addStudyGroup(group.getId(), group)
                        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("Successfully updated new group, type 'show' to see all groups")
                    } else {
                        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: group with this id not found, use 'insert'")
                    }
                } else {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: group data is invalid")
                }
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "update <key> {element} - updates element from collection with provided id"
    }
}