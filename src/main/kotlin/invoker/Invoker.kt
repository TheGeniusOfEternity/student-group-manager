package invoker

import collection.CollectionInfo
import commands.*
import kotlin.collections.HashMap

/**
 * Singleton class-handler, invokes all commands
 */
object Invoker {
    val commands: HashMap<String, Command> = HashMap()
    init {
        commands["help"] = HelpCmd()
        commands["info"] = InfoCmd()
        commands["show"] = ShowCmd()
        commands["insert"] = InsertCmd()
        commands["update"] = UpdateCmd()
        commands["remove"] = RemoveCmd()
        commands["exit"] = ExitCmd()
        commands["history"] = HistoryCmd()
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
            println("Command not found: $commandName")
        }
    }
}