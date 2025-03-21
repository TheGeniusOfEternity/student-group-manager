package commands

import collection.CollectionInfo
import handlers.WriteDataFileHandler
import receiver.Receiver

/**
 * Saves [Receiver.stdGroupCollection] into a file
 */
class SaveCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val writeDataFileHandler = WriteDataFileHandler()
            val groups = Receiver.getStudyGroups()
            if (groups.isNotEmpty()) {
                writeDataFileHandler.handle(groups, args[0])
                CollectionInfo.updateDefaultFileName(args[0])
                println("Collection saved successfully, default file is ${args[0]}")
            } else {
                println("No groups found to save")
            }

        } else if (args.isEmpty()) {
            val writeDataFileHandler = WriteDataFileHandler()
            val groups = Receiver.getStudyGroups()
            if (groups.isNotEmpty()) {
                writeDataFileHandler.handle(groups, CollectionInfo.getDefaultFileName())
                println("Collection saved successfully, default file is ${CollectionInfo.getDefaultFileName()}")
            } else {
                println("No groups found to save")
            }
        }
        else {
            println("save error: invalid arguments.")
        }
    }

    override fun describe() {
        println("save <filename> - saves collection into a file")
    }
}