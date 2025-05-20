package dao

import collection.User
import handlers.DatabaseHandler
import handlers.IOHandler
import java.sql.Connection

/**
 * User data access object
 */
class UserDao(private val connection: Connection): Dao<User> {
    override fun insert(entity: User): Int? {
        val stmt = connection.prepareStatement(
            "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id"
        )
        stmt.setString(1, entity.username)
        stmt.setString(2, entity.passwordHash)
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getInt("id") else return null
    }

    override fun update(entity: User) {
        if (entity.id != null) {
            val stmt = connection.prepareStatement(
                "UPDATE ${DatabaseHandler.dbSchema}.users SET username = ?, password_hash = ? WHERE id = ?"
            )
            stmt.setString(1, entity.username)
            stmt.setString(2, entity.passwordHash)
            stmt.setInt(3, entity.id!!)
            stmt.executeUpdate()
        } else IOHandler printInfoLn "update error: userId is not specified"
    }

    override fun delete(id: Int) {
        val stmt = connection.prepareStatement("DELETE FROM ${DatabaseHandler.dbSchema}.users WHERE id = ?")
        stmt.setInt(1, id)
        stmt.executeUpdate()
    }

    override fun getAll(): List<User> {
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM ${DatabaseHandler.dbSchema}.users")
        val list = mutableListOf<User>()
        while (rs.next()) {
            list.add(User(
                id = rs.getInt("id"),
                username = rs.getString("username"),
                passwordHash = rs.getString("password_hash"),
            ))
        }
        stmt.close()
        return list
    }

    override fun getById(id: Int): User? {
        val stmt = connection.prepareStatement("SELECT * FROM ${DatabaseHandler.dbSchema}.users WHERE id = ?")
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()
        return if (rs.next()) {
            User(
                id = rs.getInt("id"),
                username = rs.getString("username"),
                passwordHash = rs.getString("password_hash")
            )
        } else null
    }

    fun getByUsername(username: String): User? {
        val stmt = connection.prepareStatement("SELECT * FROM ${DatabaseHandler.dbSchema}.users WHERE username = ?")
        stmt.setString(1, username)
        val rs = stmt.executeQuery()
        return if (rs.next()) {
            User(
                id = rs.getInt("id"),
                username = rs.getString("username"),
                passwordHash = rs.getString("password_hash")
            )
        } else null
    }
}