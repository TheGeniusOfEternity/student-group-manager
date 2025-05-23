package dao

import collection.User
import handlers.DatabaseHandler
import handlers.IOHandler
import java.sql.Connection

/**
 * User data access object
 */
object UserDao: Dao<User> {
    private val connection: Connection = DatabaseHandler.connection!!
    private val schemaName = DatabaseHandler.getSchemaName()
    init {
        require(schemaName.matches(Regex("^[a-zA-Z0-9_]+$"))) { "Invalid schema name" }
    }
    override fun insert(entity: User, userId: Int?): Int? {
        val stmt = connection.prepareStatement(
            "INSERT INTO $schemaName.users (username, password_hash) VALUES (?, ?) RETURNING id"
        )
        stmt.setString(1, entity.username)
        stmt.setString(2, entity.passwordHash)
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getInt("id") else return null
    }

    override fun update(entity: User, userId: Int?): Int {
        if (entity.id != null) {
            val stmt = connection.prepareStatement(
                "UPDATE $schemaName.users SET username = ?, password_hash = ? WHERE id = ?"
            )
            stmt.setString(1, entity.username)
            stmt.setString(2, entity.passwordHash)
            stmt.setInt(3, entity.id!!)
            stmt.executeUpdate()
        } else IOHandler printInfoLn "update error: userId is not specified"
        return -1
    }

    override fun delete(id: Int) {
        val stmt = connection.prepareStatement("DELETE FROM $schemaName.users WHERE id = ?")
        stmt.setInt(1, id)
        stmt.executeUpdate()
    }

    override fun getAll(): List<User> {
        val stmt = connection.prepareStatement("SELECT * FROM $schemaName.users")
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

    override fun getById(id: Long): User? {
        val stmt = connection.prepareStatement("SELECT * FROM $schemaName.users WHERE id = ?")
        stmt.setLong(1, id)
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