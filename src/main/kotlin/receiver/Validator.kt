package receiver

import annotations.Nested
import annotations.Pos
import collection.*
import exceptions.EmptyValueException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

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
        if (inputData.size == 11) {
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


    fun valueIsValid(input: String?, propertyName: String): Boolean {
        when (propertyName) {
            "id" -> {
                return !input.isNullOrEmpty() && input.toLong() > 0
            }
            "x" -> {
                try {
                    if (!input.isNullOrEmpty() && input.toInt() > -357) return true
                    println("insert error: value can't be empty & must be greater than -357")
                    return false
                } catch (e: NumberFormatException) {
                    println("insert error: $input is not a number")
                    return false
                }
            }
            "y" -> {
                try {
                    if (input.isNullOrEmpty()) {
                        println("insert error: value cannot be empty")
                        return false
                    }
                    input.toLong()
                    return true
                } catch (e: NumberFormatException) {
                    println("insert error: $input is not a number")
                    return false
                }
            }
            "studentsCount" -> {
                try {
                    if (!input.isNullOrEmpty() && input.toInt() > 0) return true
                    println("insert error: StudentsCount can't be empty & must be greater than zero")
                    return false
                } catch (e: NumberFormatException) {
                    println("insert error: $input is not a number")
                    return false
                }
            }
            "transferredStudents" -> {
                try {
                    if (input.isNullOrEmpty() || input.toLong() > 0) return true
                    println("insert error: TransferredStudents must be greater than zero or null")
                    return false
                } catch (e: NumberFormatException) {
                    println("insert error: $input is not a number")
                    return false
                }
            }
            "averageMark" -> {
                try {
                    if (input.isNullOrEmpty() || input.toInt() > 0) return true
                    println("insert error: AverageMark must be greater than zero or null")
                    return false
                } catch (e: NumberFormatException) {
                    println("insert error: $input is not a number")
                    return false
                }
            }

            "formOfEducation" -> {
                if (input.isNullOrEmpty()) return true
                try {
                    FormOfEducation.valueOf(input)
                    return true
                } catch (e: IllegalArgumentException) {
                    println("insert error: $input is not a valid form of education")
                    return false
                }
            }
            "name" -> {
                if (!input.isNullOrBlank()) return true
                println("insert error: name can't be empty")
                return false
            }
            "birthday" -> {
                try {
                    if (input.isNullOrEmpty()) return true
                    SimpleDateFormat("yyyy-mm-dd").parse(input)
                    return true
                } catch (e: Exception) {
                    println("insert error: incorrect date format")
                    return false
                }
            }

            "nationality" -> {
                if (input.isNullOrEmpty()) return true
                try {
                    Country.valueOf(input)
                    return true
                } catch (e: IllegalArgumentException) {
                    println("insert error: $input is not a valid country")
                    return false
                }
            }

            else -> {
                println("insert error: invalid property")
                return false
            }
        }
    }

    fun validateInsertInput(className: String, newGroupData: ArrayList<String>): StudyGroup? {
        val properties = Class.forName(className).kotlin.declaredMemberProperties.map { property ->
            val order = property.annotations.find { it is Pos } as? Pos
            property to order?.order
        }.sortedBy { it.second }
        for ((property, order) in properties) {
            if (property.annotations.contains(Nested())) {
                if (property.name == "groupAdmin") {
                    var input = ""
                    do {
                        print("${property.name.replaceFirstChar { it.uppercase() }} (Y/n): ")
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") validateInsertInput(property.returnType.toString().split("?")[0], newGroupData)
                } else {
                    validateInsertInput(property.returnType.toString().split("?")[0], newGroupData)
                }
            }
            else {
                if (property.name == "id" || property.name == "creationDate") continue
                var input: String?
                do {
                    print(property.name.replaceFirstChar { it.uppercase() })
                    if ((property.returnType.classifier as? KClass<*>)?.java?.isEnum == true) {
                        print(" (")
                        (property.returnType.classifier as? KClass<*>)?.java?.enumConstants?.forEach {
                            print("$it, ")
                        }
                        print(" )")
                    }
                    print(": ")
                    input = readln()
                } while (!valueIsValid(input, property.name))
                if (input == null) {
                    newGroupData.add("")
                } else {
                    newGroupData.add(input)
                }
            }
        }
        if (className == "collection.StudyGroup") {
            return validateGroupData(newGroupData)
        }
        return null
    }
}