package commands

import handlers.ConnectionHandler
import handlers.IOHandler
import invoker.Invoker

/**
 * Shows description of all commands
 */
class GetCommandsListCmd: Command {
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            val commandNames = ArrayList<Pair<String, String>>()
            Invoker.commands.forEach { command -> commandNames.add(Pair(command.key, command.value.describe())) }
            ConnectionHandler.handleResponse(commandNames)
        } else {
            IOHandler printInfoLn "help: too many arguments"
        }
        return
    }

    override fun describe(): String {
        return "help - shows all available commands"
    }
}