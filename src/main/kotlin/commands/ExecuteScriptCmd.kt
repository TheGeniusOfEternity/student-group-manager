package commands

import collection.CollectionInfo
import handlers.IOHandler
import java.io.File

/**
 * Executes commands from the file
 */
class ExecuteScriptCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val filename = "scripts/${args[0]}"
            if (File(filename).exists()) {
                if (CollectionInfo.getOpenedFiles().none { it.first.contains(filename) }) {
                    IOHandler.handleFileInput(filename, null)
                    IOHandler printInfo "\nEnd of executing script $filename\n\n"
                } else {
                    IOHandler printInfo "execute_script error: file ${args[0]} is already opened at the moment\n"
                }

            } else {
                IOHandler printInfo "execute_script error: file not found: $filename"
            }

        } else {
            IOHandler printInfo "execute_script error: invalid count of arguments"
        }
    }

    override fun describe() {
        IOHandler printInfo "execute_script <filename> - executes commands from the file"
    }
}