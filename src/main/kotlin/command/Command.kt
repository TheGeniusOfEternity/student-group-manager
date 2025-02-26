package command

interface Command {
    fun execute(args: List<String>)
    fun describe()
}