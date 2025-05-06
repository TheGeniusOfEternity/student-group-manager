package invoker

import commands.*
import handlers.IOHandler
import kotlin.collections.HashMap

/**
 * Singleton class-handler, invokes all commands
 *
 * @property commands List of all existing commands
 */
object Invoker {
    val commands: HashMap<String, Command> = HashMap()
    init {
        commands["help"] = HelpCmd()
        commands["exit"] = ExitCmd()
        commands["execute_script"] = ExecuteScriptCmd()
    }

    /**
     * Initiates [Command] execution
     */
    fun run(commandName: String, args: List<String>) {
        val command: Command? = this.commands[commandName]
        if (command != null) {
            command.execute(listOf(commandName) + args)
        } else {
            IOHandler printInfoLn "Unknown command $commandName"
        }
    }
}