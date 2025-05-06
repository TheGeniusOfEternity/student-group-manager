package commands
import State
import dto.CommandParam
import handlers.IOHandler

/**
 * Stops program running
 */
class ExitCmd : Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String) {
        IOHandler printInfoLn "Exiting console..."
        State.isRunning = false
    }

    override fun describe(): String {
        return "exit  - stops program running"
    }
}