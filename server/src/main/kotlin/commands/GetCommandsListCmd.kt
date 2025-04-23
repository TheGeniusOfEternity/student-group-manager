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
                if (command.key != "save" && command.key != "get_commands_list") {
                    commandNames.add(CommandInfoDto(command.key, command.value.describe(), command.value.paramTypeName))
                }
            }
            ConnectionHandler.handleResponse<CommandInfoDto?>(commandNames)
        } else {
            IOHandler.responsesThread.add("get_commands_list error: too many arguments")
        }
    }

    override fun describe(): String {
        return "get_commands_list - loads all commands from server"
    }
}