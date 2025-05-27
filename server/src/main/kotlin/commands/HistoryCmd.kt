package commands

import collection.CollectionInfo
import collection.CollectionInfo.commandsList
import dto.CommandParam
import handlers.IOHandler


/**
 * Shows [CollectionInfo.commandsHistory] - list of last 11 executed commands
 */
class HistoryCmd : Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
            .add(Pair("Commands history:\n${commandsList()}", correlationId))
    }

    override fun describe(): String {
        return "history - show 11 last executed commands"
    }
}