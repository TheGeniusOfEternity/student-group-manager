package handlers

import collection.Country
import collection.FormOfEducation
import java.sql.Connection
import java.sql.DriverManager

object DatabaseHandler {
    val dbSchema: String? = State.credentials["DB_SCHEMA"]
    private val dbUsername: String? = State.credentials["DB_USERNAME"]
    private val dbPassword: String? = State.credentials["DB_PASSWORD"]
    var connection: Connection? = null

    fun setUp() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres?currentSchema=$dbSchema", dbUsername, dbPassword)
            val statement = connection!!.createStatement()
            statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS $dbSchema AUTHORIZATION $dbUsername;")
            IOHandler printInfoLn "Connected to database"
            IOHandler printInfoLn "Schema '$dbSchema' created or already exists."
            statement.close()

            loadTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTables() {
        val statement = connection!!.createStatement()
        statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS $dbSchema.users(
                id SERIAL PRIMARY KEY NOT NULL,
                username VARCHAR(100) NOT NULL,
                password_hash VARCHAR(255) NOT NULL
            );
        """.trimIndent())
        IOHandler printInfoLn "Table 'users' created or already exists."
        statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS $dbSchema.study_groups(
                id SERIAL PRIMARY KEY NOT NULL,
                name VARCHAR(100) NOT NULL,
                coordinates_x INT NOT NULL,
                coordinates_y BIGINT NOT NULL,
                students_count INT NOT NULL,
                transferred_students INT,
                average_mark INT,
                form_of_education VARCHAR(100),
                group_admin_name VARCHAR(100),
                group_admin_birthday DATE,
                group_admin_nationality VARCHAR(100),
                creation_date DATE NOT NULL,
                user_id INT NOT NULL,
            
                CHECK (coordinates_x > -357),
                CHECK (form_of_education IN (
                    '${FormOfEducation.DISTANCE_EDUCATION}',
                    '${FormOfEducation.FULL_TIME_EDUCATION}',
                    '${FormOfEducation.EVENING_CLASSES}'
                ) OR form_of_education IS NULL),
                CHECK (
                    (
                        group_admin_name IS NULL AND
                        group_admin_birthday IS NULL AND
                        group_admin_nationality IS NULL
                    ) 
                    OR
                    (
                        group_admin_name IS NOT NULL AND
                        group_admin_nationality IN (
                            '${Country.RUSSIA}',
                            '${Country.GERMANY}',
                            '${Country.SOUTH_KOREA}'
                        ) OR group_admin_nationality IS NULL
                    )
                ),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """.trimIndent())
        IOHandler printInfoLn "Table 'study_groups' created or already exists."
    }
}