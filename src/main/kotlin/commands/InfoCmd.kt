package commands
import receiver.Receiver
import collection.CollectionInfo

/**
 * Shows info about Collection, storing in [Receiver]
 */
class InfoCmd: Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            print(CollectionInfo.toString())
        } else {
            println("info: Too many arguments")
        }
    }

    override fun describe() {
        println("info - shows information about collection (type, data initialized, elements count)")
    }
}