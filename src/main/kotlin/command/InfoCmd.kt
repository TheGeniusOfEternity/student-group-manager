package command

import receiver.Receiver

class InfoCmd: Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            print(Receiver.getCollectionInfo().toString())
        } else {
            println("info: Too many arguments")
        }
    }

    override fun describe() {
        println("info - shows information about collection (type, data initialized, elements count)")
    }
}