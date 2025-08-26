package dao

import collection.*
import handlers.DatabaseHandler
import receiver.Receiver
import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.text.SimpleDateFormat
import java.time.LocalDate

object StudyGroupDao : Dao<StudyGroup> {
    private val connection: Connection = DatabaseHandler.connection!!
    private val schemaName = DatabaseHandler.getSchemaName()
    init {
        require(schemaName.matches(Regex("^[a-zA-Z0-9_]+$"))) { "Invalid schema name" }
    }
    override fun insert(entity: StudyGroup, userId: Int?): Int? {
        if (userId == null) return null
        val stmt = connection.prepareStatement(
            """INSERT INTO $schemaName.study_groups (
                id,
                name,
                coordinates_x,
                coordinates_y,
                students_count,
                transferred_students,
                average_mark,
                form_of_education,
                group_admin_name,
                group_admin_birthday,
                group_admin_nationality,
                creation_date,
                user_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id""".trimIndent()
        )
        fillPlaceholders(stmt, entity, userId)
        try {
            val rs = stmt.executeQuery()
            return if (rs.next()) rs.getInt("id") else return null
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    override fun update(entity: StudyGroup, userId: Int?): Int? {
        if (userId == null) return null
        val stmt = connection.prepareStatement(
            """UPDATE $schemaName.study_groups SET 
                id = ?,
                name = ?,
                coordinates_x = ?,
                coordinates_y = ?,
                students_count = ?,
                transferred_students = ?,
                average_mark = ?,
                form_of_education = ?,
                group_admin_name = ?,
                group_admin_birthday = ?,
                group_admin_nationality = ?,
                creation_date = ?,
                user_id = ?
            WHERE id = ? RETURNING id""".trimIndent()
        )
        fillPlaceholders(stmt, entity, userId)
        stmt.setLong(14, entity.getId())
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getInt("id") else return null
    }

    override fun delete(id: Int) {
        val stmt = connection.prepareStatement("DELETE FROM $schemaName.study_groups WHERE id = ?")
        stmt.setInt(1, id)
        stmt.executeUpdate()
    }

    override fun getAll(): List<StudyGroup> {
        val stmt = connection.prepareStatement("SELECT * FROM $schemaName.study_groups")
        val rs = stmt.executeQuery()
        val list = mutableListOf<StudyGroup>()
        while (rs.next()) {
            val group = loadGroupFromDatabase(rs)
            val userId = rs.getInt("user_id")
            val user = Receiver.getUsers().values.find { user -> user.id == userId }
            group?.setOwnerName(user?.username)
            if (group != null) list.add(group)
        }
        stmt.close()
        return list
    }

    override fun getById(id: Long): StudyGroup? {
        val stmt = connection.prepareStatement("SELECT * FROM $schemaName.study_groups WHERE id = ?")
        stmt.setLong(1, id)
        val rs = stmt.executeQuery()
        return if (rs.next()) loadGroupFromDatabase(rs) else null
    }

    private fun loadGroupFromDatabase(rs: ResultSet): StudyGroup? {
        try {
            return StudyGroup(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                coordinates = Coordinates(
                    x = rs.getInt("coordinates_x"),
                    y = rs.getLong("coordinates_y"),
                ),
                studentsCount = rs.getInt("students_count"),
                transferredStudents = if (rs.getLong("transferred_students") == 0L) null
                else rs.getLong("transferred_students"),
                averageMark = if (rs.getInt("average_mark") == 0) null
                else rs.getInt("average_mark"),
                formOfEducation = try {
                    FormOfEducation.valueOf(rs.getString("form_of_education") ?: "")
                } catch (e: IllegalArgumentException) { null },
                groupAdmin = if (rs.getString("group_admin_name") == null) null
                else Person(
                    name = rs.getString("group_admin_name")!!,
                    birthday = try {
                        SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("group_admin_birthday"))
                    } catch (e: Exception) { null },
                    nationality = try {
                        Country.valueOf(rs.getString("group_admin_nationality"))
                    } catch (e: IllegalArgumentException) { null },
                ),
                creationDate = LocalDate.parse(rs.getString("creation_date")),
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun fillPlaceholders(stmt: PreparedStatement, entity: StudyGroup, userId: Int) {
        stmt.setLong(1, entity.getId())
        stmt.setString(2, entity.name)
        stmt.setInt(3, entity.getCoordinate().getX())
        stmt.setLong(4, entity.getCoordinate().getY())
        stmt.setInt(5, entity.getStudentsCount())
        if (entity.getTransferredStudents() != null) {
            stmt.setLong(6, entity.getTransferredStudents()!!)
        } else stmt.setNull(6, Types.BIGINT)
        if (entity.getAverageMark() != null) {
            stmt.setInt(7, entity.getAverageMark()!!)
        } else stmt.setNull(7, Types.INTEGER)
        if (entity.getFormOfEducation() != null) {
            stmt.setString(8, entity.getFormOfEducation().toString())
        } else stmt.setNull(8, Types.VARCHAR)
        if (entity.getGroupAdmin() != null) {
            stmt.setString(9, entity.getGroupAdmin()!!.getName())
            stmt.setString(11, entity.getGroupAdmin()!!.getNationality().toString())
            if (entity.getGroupAdmin()!!.getBirthday() != null) {
                stmt.setDate(10, Date(entity.getGroupAdmin()!!.getBirthday()!!.time))
            } else stmt.setNull(10, Types.DATE)
        } else {
            stmt.setNull(9, Types.VARCHAR)
            stmt.setNull(10, Types.DATE)
            stmt.setNull(11, Types.VARCHAR)
        }
        stmt.setDate(12, Date.valueOf(entity.getCreationDate()))
        stmt.setInt(13, userId)
    }

}