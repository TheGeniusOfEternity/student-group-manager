package validators

import core.Property
import collection.Country
import collection.FormOfEducation
import java.text.SimpleDateFormat

/**
 * Class for validation single [Property]
 */

class PropertyValidator: Validator<Property> {
    /**
     * Checks if [Property] value is valid for its name
     * @param data [Property] with name of property and its value
     * @return true, if value is valid, false otherwise
     */
    override fun validateData(data: Property): Boolean {
        when (data.first) {
            "id" -> {
                try {
                    return (!data.second.isNullOrEmpty() && data.second?.toLong()!! > 0)
                } catch (e: NumberFormatException) {
                    println("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "creationDate" -> {
                println("validation error: creationDate can't be set")
                return false
            }
            "x" -> {
                try {
                    if (!data.second.isNullOrEmpty() && data.second?.toInt()!! > -357) return true
                    println("validation error: value can't be empty & must be greater than -357")
                    return false
                } catch (e: NumberFormatException) {
                    println("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "y" -> {
                try {
                    if (data.second.isNullOrEmpty()) {
                        println("validation error: value cannot be empty")
                        return false
                    }
                    data.second?.toLong()
                    return true
                } catch (e: NumberFormatException) {
                    println("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "studentsCount" -> {
                try {
                    if (!data.second.isNullOrEmpty() && data.second?.toInt()!! > 0) return true
                    println("validation error: StudentsCount can't be empty & must be greater than zero")
                    return false
                } catch (e: NumberFormatException) {
                    println("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "transferredStudents" -> {
                try {
                    if (data.second.isNullOrEmpty() || data.second?.toLong()!! > 0) return true
                    println("validation error: TransferredStudents must be greater than zero or null")
                    return false
                } catch (e: NumberFormatException) {
                    println("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "averageMark" -> {
                try {
                    if (data.second.isNullOrEmpty() || data.second?.toInt()!! > 0) return true
                    println("validation error: AverageMark must be greater than zero or null")
                    return false
                } catch (e: NumberFormatException) {
                    println("validation error: ${data.second} is not a number")
                    return false
                }
            }

            "formOfEducation" -> {
                if (data.second.isNullOrEmpty()) return true
                try {
                    FormOfEducation.valueOf(data.second!!.uppercase())
                    return true
                } catch (e: IllegalArgumentException) {
                    println("validation error: ${data.second} is not a valid form of education")
                    return false
                }
            }
            "name" -> {
                if (!data.second.isNullOrBlank()) return true
                println("validation error: name can't be empty")
                return false
            }
            "birthday" -> {
                try {
                    if (data.second.isNullOrEmpty()) return true
                    val formatter = SimpleDateFormat("dd.MM.yyyy")
                    formatter.parse(data.second)
                    return true
                } catch (e: Exception) {
                    println("validation error: incorrect date format\nShould use dd.MM.yyyy format")
                    return false
                }
            }

            "nationality" -> {
                if (data.second.isNullOrEmpty()) return true
                try {
                    Country.valueOf(data.second!!.uppercase())
                    return true
                } catch (e: IllegalArgumentException) {
                    println("validation error: ${data.second} is not a valid country")
                    return false
                }
            }

            else -> {
                println("validation error: invalid property ${data.first} ${data.second}")
                return false
            }
        }
    }
}