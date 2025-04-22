package commands
import receiver.Receiver
import collection.CollectionInfo
import dto.CommandParam
import handlers.ConnectionHandler
import handlers.IOHandler
import java.util.ArrayList

/**
 * Shows info about Collection, storing in [Receiver]
 */
class InfoCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>) {
        if (args.isEmpty()) {
            val collectionInfo = ArrayList<String>()
            collectionInfo.add(CollectionInfo.toString())
            ConnectionHandler.handleResponse(collectionInfo)
        } else {
            IOHandler printInfoLn "info: Too many arguments"
        }
    }

    override fun describe(): String {
        return "info - shows information about collection (type, data initialized, elements count)"
    }
}