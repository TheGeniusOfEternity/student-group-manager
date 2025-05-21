package commands

import dto.CommandParam
import handlers.IOHandler
import services.JwtTokenService

class RefreshTokenCmd : Command {
    override val paramTypeName = "String"

    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 2) {
            val token = JwtTokenService.decodeToken((args[0] as CommandParam.StringParam).value!!)
            IOHandler.printInfoLn(token.body["typ"].toString())
            if (token.body["typ"] == "refresh") {
                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                    .add(
                        "${JwtTokenService.generateAccessToken(token.body.subject)}#" +
                                JwtTokenService.generateRefreshToken(token.body.subject)
                    )
            }
        } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("refresh_token: invalid count of arguments - ${args.size}")
    }

    override fun describe(): String {
        return "refresh_token: refreshes access & refresh tokens"
    }
}