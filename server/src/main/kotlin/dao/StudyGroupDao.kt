package dao

import collection.*
import handlers.DatabaseHandler
import handlers.IOHandler
import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.time.LocalDate

class StudyGroupDao(private val connection: Connection) : Dao<StudyGroup> {
    override fun insert(entity: StudyGroup, userId: Int?): Int? {
        if (userId == null) return null
        val stmt = connection.prepareStatement(
            """INSERT INTO ${DatabaseHandler.dbSchema}.study_groups (
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
            IOHandler printInfoLn rs.toString()
            return if (rs.next()) rs.getInt("id") else return null
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }

    override fun update(entity: StudyGroup, userId: Int?): Int? {
        if (userId == null) return null
        val stmt = connection.prepareStatement(
            """UPDATE ${DatabaseHandler.dbSchema}.study_groups SET 
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
                userId = ?
            WHERE id = ? RETURNING id""".trimIndent()
        )
        fillPlaceholders(stmt, entity, userId)
        stmt.setLong(14, entity.getId())
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getInt("id") else return null
    }

    override fun delete(id: Int) {
        val stmt = connection.prepareStatement("DELETE FROM ?.study_groups WHERE id = ?")
        stmt.setString(1, DatabaseHandler.dbSchema)
        stmt.setInt(2, id)
        stmt.executeUpdate()
    }

    override fun getAll(): List<StudyGroup> {
        val stmt = connection.prepareStatement("SELECT * FROM ${DatabaseHandler.dbSchema}.study_groups")
        val rs = stmt.executeQuery()
        val list = mutableListOf<StudyGroup>()
        while (rs.next()) {
            val group = loadGroupFromDatabase(rs)
            if (group != null) list.add(group)
        }
        stmt.close()
        return list
    }

    override fun getById(id: Long): StudyGroup? {
        val stmt = connection.prepareStatement("SELECT * FROM ?.study_groups WHERE id = ?")
        stmt.setString(1, DatabaseHandler.dbSchema)
        stmt.setLong(2, id)
        val rs = stmt.executeQuery()
        return if (rs.next()) loadGroupFromDatabase(rs) else null
    }

    fun getUsernameByGroupId(id: Long): String? {
        val stmt = connection.prepareStatement("""
            SELECT ${DatabaseHandler.dbSchema}.users.username 
            FROM ${DatabaseHandler.dbSchema}.study_groups 
            JOIN ${DatabaseHandler.dbSchema}.users 
            ON users.id = user_id WHERE ${DatabaseHandler.dbSchema}.study_groups.id = ?
        """.trimIndent())
        stmt.setLong(1, id)
        val rs = stmt.executeQuery()
        return if (rs.next()) rs.getString("username") else null
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
                    birthday = Date.valueOf(rs.getString("group_admin_birthday")),
                    nationality = Country.valueOf(rs.getString("group_admin_nationality"))
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