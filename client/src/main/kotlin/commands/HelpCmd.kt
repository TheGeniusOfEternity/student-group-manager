package commands

import handlers.IOHandler
import invoker.Invoker

/**
 * Shows description of all commands
 */
class HelpCmd: Command {
    override val paramTypeName = null
    override val description: String = "help - shows all available commands"
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            IOHandler printInfoLn "Available commands:"
            Invoker.commands.toSortedMap().forEach{ it.value.describe()}
        } else {
            IOHandler printInfoLn "help: too many arguments"
        }
    }

    override fun describe() {
        IOHandler printInfoLn description
    }
}