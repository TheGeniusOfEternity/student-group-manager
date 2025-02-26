package command

import receiver.Receiver

class ShowCmd: Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            Receiver.getStudyGroups().forEach{
                println(it.value.toString())
                println()
            }
        } else {
            println("show: Too many arguments")
        }
    }

    override fun describe() {
        println("show - shows all elements from collection")
    }
}