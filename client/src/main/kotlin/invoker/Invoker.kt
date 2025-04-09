package invoker

import collection.CollectionInfo
import commands.*
import handlers.ConnectionHandler
import handlers.IOHandler
import kotlin.collections.HashMap

/**
 * Singleton class-handler, invokes all commands
 *
 * @property commands List of all existing commands
 */
object Invoker {
    val commands: HashMap<String, Command?> = HashMap()
    init {
        commands["help"] = HelpCmd()
        commands["exit"] = ExitCmd()
    }

    /**
     * Initiates [Command] execution
     */
    fun run(commandName: String, args: List<String>) {
        val command: Command? = this.commands[commandName]
        if (command != null) {
            CollectionInfo.updateCommandHistory(commandName)
            command.execute(args)
        } else {
            IOHandler printInfoLn "Unknown command $commandName"
        }
    }
}