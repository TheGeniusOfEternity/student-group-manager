package dao

import collection.User
import handlers.DatabaseHandler
import handlers.IOHandler
import java.sql.Connection

/**
 * User data access object
 */
class UserDao(private val connection: Connection): Dao<User> {
    override fun insert(entity: User, userId: Int?): Int? {
        val stmt = connection.prepareStatement(
            "INSERT INTO ${DatabaseHandler.dbSchema}.users (username, password_hash) VALUES (?, ?) RETURNING id"
        )
        stmt.setString(1, entity.username)
        stmt.setString(2, entity.passwordHash)
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getInt("id") else return null
    }

    override fun update(entity: User, userId: Int?): Int {
        if (entity.id != null) {
            val stmt = connection.prepareStatement(
                "UPDATE ?.users SET username = ?, password_hash = ? WHERE id = ?"
            )
            stmt.setString(1, DatabaseHandler.dbSchema)
            stmt.setString(2, entity.username)
            stmt.setString(3, entity.passwordHash)
            stmt.setInt(4, entity.id!!)
            stmt.executeUpdate()
        } else IOHandler printInfoLn "update error: userId is not specified"
        return -1
    }

    override fun delete(id: Int) {
        val stmt = connection.prepareStatement("DELETE FROM ?.users WHERE id = ?")
        stmt.setString(1, DatabaseHandler.dbSchema)
        stmt.setInt(2, id)
        stmt.executeUpdate()
    }

    override fun getAll(): List<User> {
        val stmt = connection.prepareStatement("SELECT * FROM ?.users")
        stmt.setString(1, DatabaseHandler.dbSchema)
        val rs = stmt.executeQuery()
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
        val stmt = connection.prepareStatement("SELECT * FROM ?.users WHERE id = ?")
        stmt.setString(1, DatabaseHandler.dbSchema)
        stmt.setInt(2, id)
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