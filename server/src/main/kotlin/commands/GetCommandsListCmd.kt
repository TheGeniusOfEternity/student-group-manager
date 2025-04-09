package commands

import handlers.ConnectionHandler
import handlers.IOHandler
import invoker.Invoker

/**
 * Shows description of all commands
 */
class GetCommandsListCmd: Command {
    override fun execute(args: List<Any?>) {
        if (args.isEmpty()) {
            val commandNames = ArrayList<String>()
            Invoker.commands.forEach { command -> commandNames.add(command.key) }
            ConnectionHandler.handleResponse(commandNames)
        } else {
            IOHandler printInfoLn "help: too many arguments"
        }
        return
    }

    override fun describe() {
        IOHandler printInfoLn "help - shows all available commands"
    }
}