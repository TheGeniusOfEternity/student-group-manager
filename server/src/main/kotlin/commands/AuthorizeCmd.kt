package commands

import collection.User
import dao.UserDao
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver
import services.JwtTokenService
import java.security.MessageDigest

class AuthorizeCmd : Command {
    override val paramTypeName = "String"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        var responseMsg = ""
        if (args.size == 1) {
            val username = (args[0] as CommandParam.StringParam).value?.split(":")?.get(0)
            val password = (args[0] as CommandParam.StringParam).value?.split(":")?.get(1)
            if (username != null && password != null) {
                val currentUser = User(null, username, hashPassword(password))
                try {
                    val existedUser = Receiver.getUsers().entries.find { user -> user.value.username == username }?.value
                    if (existedUser == null)  currentUser.id = UserDao.insert(currentUser)
                    else if (existedUser.passwordHash == currentUser.passwordHash) currentUser.id = existedUser.id
                    if (currentUser.id != null) {
                        val accessToken = JwtTokenService.generateAccessToken(currentUser.id!!.toString())
                        val refreshToken = JwtTokenService.generateRefreshToken(currentUser.id!!.toString())
                        responseMsg = "$accessToken#&#$refreshToken"
                    } else responseMsg = "authorize: incorrect password"
                } catch (e: Exception) {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                        .add(Pair("authorize: ${e.printStackTrace()}", correlationId))
                }
            } else responseMsg = "authorize: username or password are missing"
        } else responseMsg = "authorize: username or password are missing"
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "authorize: sign in / sign up user in database"
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("MD2")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}