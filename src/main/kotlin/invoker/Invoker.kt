package invoker

import command.*
import receiver.Receiver
import kotlin.collections.HashMap


object Invoker {
    val commands: HashMap<String, Command> = HashMap()
    init {
        commands["help"] = HelpCmd()
        commands["info"] = InfoCmd()
        commands["show"] = ShowCmd()
        commands["exit"] = ExitCmd()
    }
    fun run(commandName: String, args: List<String>) {
        val command: Command? = this.commands[commandName]
        if (command != null) {
            Receiver.getCollectionInfo().updateCommandHistory(commandName)
            command.execute(args)
        } else {
            println("Command not found: $commandName")
        }
    }
}