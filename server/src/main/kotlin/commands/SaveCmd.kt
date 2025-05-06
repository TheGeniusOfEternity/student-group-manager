package commands

import collection.CollectionInfo
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Saves [Receiver.stdGroupCollection] into a file
 */
class SaveCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            val groups = Receiver.getStudyGroups()
            if (groups.isNotEmpty()) {
                IOHandler.handleFileOutput(groups, (args[0] as String))
                CollectionInfo.updateDefaultFileName((args[0] as String))
                IOHandler printInfo "Collection saved successfully, default file is ${(args[0] as String)}"
            } else {
                IOHandler printInfo "No groups found to save\n"
            }

        } else if (args.isEmpty()) {
            val groups = Receiver.getStudyGroups()
            if (groups.isNotEmpty()) {
                IOHandler.handleFileOutput(groups, CollectionInfo.getDefaultFileName())
                IOHandler printInfo "Collection saved successfully, default file is ${CollectionInfo.getDefaultFileName()}"
            } else {
                IOHandler printInfo "No groups found to save\n"
            }
        }
        else {
            IOHandler printInfo "save error: invalid arguments.\n"
        }
    }

    override fun describe(): String {
        return "save <filename> - saves collection into a file"
    }
}