package commands

import dto.CommandInfoDto
import handlers.ConnectionHandler
import handlers.IOHandler
import invoker.Invoker

/**
 * Shows description of all commands
 */
class GetCommandsListCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<Any?>) {
        if (args.isEmpty()) {
            val commandNames = ArrayList<CommandInfoDto>()
            Invoker.commands.forEach { command ->
                commandNames.add(CommandInfoDto(command.key, command.value.describe(), command.value.paramTypeName))
            }
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