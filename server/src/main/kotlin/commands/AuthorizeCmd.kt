package commands

import collection.User
import dao.UserDao
import dto.CommandParam
import handlers.DatabaseHandler
import handlers.IOHandler
import services.JwtTokenService
import java.security.MessageDigest

class AuthorizeCmd : Command {
    override val paramTypeName = "String"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            val username = (args[0] as CommandParam.StringParam).value?.split(":")?.get(0)
            val password = (args[0] as CommandParam.StringParam).value?.split(":")?.get(1)
            if (username != null && password != null) {
                val user = User(null, username, hashPassword(password))
                try {
                    val connection = DatabaseHandler.connection
                    if (connection != null) {
                        val existedUser = UserDao(connection).getByUsername(username)
                        if (existedUser == null)  user.id = UserDao(connection).insert(user)
                        else if (existedUser.passwordHash == user.passwordHash) user.id = existedUser.id
                        if (user.id != null) {
                            val accessToken = JwtTokenService.generateAccessToken(user.id!!.toString())
                            val refreshToken = JwtTokenService.generateRefreshToken(user.id!!.toString())
                            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("$accessToken:$refreshToken")
                        } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("authorize: incorrect password")
                    } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("authorize: no connection to database")
                } catch (e: Exception) {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("authorize: ${e.cause}")
                }
            } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("authorize: username or password are missing")
        } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("authorize: invalid count of arguments")
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