package commands

import dto.CommandInfoDto
import dto.CommandParam
import handlers.ConnectionHandler
import handlers.IOHandler
import invoker.Invoker

/**
 * Shows description of all commands
 */
class GetCommandsListCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>) {
        if (args.isEmpty()) {
            val commandNames = ArrayList<CommandInfoDto?>()
            Invoker.commands.forEach { command ->
                commandNames.add(CommandInfoDto(command.key, command.value.describe(), command.value.paramTypeName))
            }
            ConnectionHandler.handleResponse<CommandInfoDto?>(commandNames)
        } else {
            IOHandler printInfoLn "help: too many arguments"
        }
        return
    }

    override fun describe(): String {
        return "help - shows all available commands"
    }
}