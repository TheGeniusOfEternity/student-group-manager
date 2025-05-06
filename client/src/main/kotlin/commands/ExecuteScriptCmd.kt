package commands

import core.State
import handlers.IOHandler
import java.io.File

/**
 * Executes commands from the file
 */
class ExecuteScriptCmd: Command {
    override val paramTypeName = "string"
    override val description: String = "execute_script <filename> - executes commands from the file"
    override fun execute(args: List<String>) {
        if (args.size == 2) {
            val filename = "scripts/${args[1]}"
            if (File(filename).exists()) {
                if (State.getOpenedFiles().none { it.first.contains(filename) }) {
                    IOHandler.handleFileInput(filename, null)
                    IOHandler printInfoLn "\nEnd of executing script $filename\n\n"
                } else IOHandler printInfoLn "execute_script error: file ${args[1]} is already opened at the moment\n"
            } else IOHandler printInfoLn "execute_script error: file not found: '$filename'"
        } else IOHandler printInfoLn "execute_script error: invalid count of arguments"
    }

    override fun describe() {
        val connectionRequirement = if (!State.connectedToServer) ", for usage connect to server" else ""
        IOHandler printInfoLn description + connectionRequirement
    }
}