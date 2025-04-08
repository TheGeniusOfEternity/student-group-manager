package commands

import collection.CollectionInfo
import collection.CollectionInfo.commandsList
import handlers.IOHandler


/**
 * Shows [CollectionInfo.commandsHistory] - list of last 11 executed commands
 */
class HistoryCmd : Command {
    override fun execute(args: List<String>) {
        IOHandler printInfo "Commands history:\n${commandsList()}"
    }

    override fun describe() {
        IOHandler printInfo "history - show 11 last executed commands"
    }
}