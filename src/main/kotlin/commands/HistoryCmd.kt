package commands

import collection.CollectionInfo
import collection.CollectionInfo.commandsList


/**
 * Shows [CollectionInfo.commandsHistory] - list of last 11 executed commands
 */
class HistoryCmd : Command {
    override fun execute(args: List<String>) {
        println("Commands history:\n${commandsList()}")
    }

    override fun describe() {
        println("history - show 11 last executed commands")
    }
}