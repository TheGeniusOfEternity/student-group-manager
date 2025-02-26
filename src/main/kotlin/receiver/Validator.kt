package receiver

import collection.*
import exceptions.EmptyValueException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Validator {
    fun validateGroupData(inputData: ArrayList<String>): StudyGroup {
        val id: String = inputData[0].ifEmpty { throw EmptyValueException("Id can't be empty") }
        val name: String = inputData[1].ifEmpty { throw EmptyValueException("Name can't be empty") }
        val x: String = inputData[2].ifEmpty { throw EmptyValueException("x coordinate can't be empty") }
        val y: String = inputData[3].ifEmpty { throw EmptyValueException("y coordinate can't be empty") }
        val coords: Coordinates = Coordinates(x.toInt(), y.toLong())
        val stdCount: String = inputData[4].ifEmpty { throw EmptyValueException("students count can't be empty") }
        val transferredStudents: Long? = inputData[5].toLongOrNull()
        val averageMark: Int? = inputData[6].toIntOrNull()
        val formOfEducation: FormOfEducation? = if (inputData[7].isNotEmpty()) FormOfEducation.valueOf(inputData[7]) else null
        val admin: Person?
        if (inputData.size > 8) {
            val adminName: String = inputData[8].ifEmpty { throw EmptyValueException("Person name can't be empty") }
            val adminBirth: Date? = if (inputData[9].isNotEmpty())
                                        SimpleDateFormat("yyyy-mm-dd").parse(inputData[9])
                                    else null
            val nationality: Country? = if (inputData[10].isNotEmpty()) Country.valueOf(inputData[10]) else null
            admin = Person(adminName, adminBirth, nationality)
        } else {
            admin = null
        }

        return StudyGroup(
            id = id.toLong(),
            name = name,
            coordinates = coords,
            studentsCount = stdCount.toInt(),
            transferredStudents = transferredStudents,
            averageMark = averageMark,
            formOfEducation = formOfEducation,
            groupAdmin = admin
        )
    }
}