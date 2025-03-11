package commands

import invoker.Invoker

/**
 * Shows description of all commands
 */
class HelpCmd: Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            println("Available commands:")
            Invoker.commands.toSortedMap().forEach{ it.value.describe()}
        } else {
            println("help: too many arguments")
        }
    }

    override fun describe() {
        println("help - shows all available commands")
    }
}