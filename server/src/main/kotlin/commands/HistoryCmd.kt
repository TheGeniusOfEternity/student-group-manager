package commands

import collection.CollectionInfo
import collection.CollectionInfo.commandsList
import dto.CommandParam
import handlers.ConnectionHandler
import handlers.IOHandler


/**
 * Shows [CollectionInfo.commandsHistory] - list of last 11 executed commands
 */
class HistoryCmd : Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>) {
        IOHandler.responsesThread.add("Commands history:\n${commandsList()}")
    }

    override fun describe(): String {
        return "history - show 11 last executed commands"
    }
}