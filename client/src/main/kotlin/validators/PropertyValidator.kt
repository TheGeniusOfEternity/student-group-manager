package validators

import core.Property
import collection.Country
import collection.FormOfEducation
import handlers.IOHandler
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
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "creationDate" -> {
                IOHandler.responsesThreads.add("validation error: creationDate can't be set")
                return false
            }
            "x" -> {
                try {
                    if (!data.second.isNullOrEmpty() && data.second?.toInt()!! > -357) return true
                    IOHandler.responsesThreads.add("validation error: value can't be empty & must be greater than -357")
                    return false
                } catch (e: NumberFormatException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "y" -> {
                try {
                    if (data.second.isNullOrEmpty()) {
                        IOHandler.responsesThreads.add("validation error: value cannot be empty")
                        return false
                    }
                    data.second?.toLong()
                    return true
                } catch (e: NumberFormatException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "studentsCount" -> {
                try {
                    if (!data.second.isNullOrEmpty() && data.second?.toInt()!! > 0) return true
                    IOHandler.responsesThreads.add("validation error: StudentsCount can't be empty & must be greater than zero")
                    return false
                } catch (e: NumberFormatException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "transferredStudents" -> {
                try {
                    if (data.second.isNullOrEmpty() || data.second?.toLong()!! > 0) return true
                    IOHandler.responsesThreads.add("validation error: TransferredStudents must be greater than zero or null")
                    return false
                } catch (e: NumberFormatException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a number")
                    return false
                }
            }
            "averageMark" -> {
                try {
                    if (data.second.isNullOrEmpty() || data.second?.toInt()!! > 0) return true
                    IOHandler.responsesThreads.add("validation error: AverageMark must be greater than zero or null")
                    return false
                } catch (e: NumberFormatException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a number")
                    return false
                }
            }

            "formOfEducation" -> {
                if (data.second.isNullOrEmpty()) return true
                try {
                    FormOfEducation.valueOf(data.second!!.uppercase())
                    return true
                } catch (e: IllegalArgumentException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a valid form of education")
                    return false
                }
            }
            "name" -> {
                if (!data.second.isNullOrBlank()) return true
                IOHandler.responsesThreads.add("validation error: name can't be empty")
                return false
            }
            "birthday" -> {
                try {
                    if (data.second.isNullOrEmpty()) return true
                    val formatter = SimpleDateFormat("yyyy-mm-dd")
                    formatter.parse(data.second)
                    return true
                } catch (e: Exception) {
                    IOHandler.responsesThreads.add("validation error: incorrect date format\nShould use yyyy-mm-dd format")
                    return false
                }
            }

            "nationality" -> {
                if (data.second.isNullOrEmpty()) return true
                try {
                    Country.valueOf(data.second!!.uppercase())
                    return true
                } catch (e: IllegalArgumentException) {
                    IOHandler.responsesThreads.add("validation error: ${data.second} is not a valid country")
                    return false
                }
            }

            else -> {
                IOHandler.responsesThreads.add("validation error: invalid property ${data.first} ${data.second}")
                return false
            }
        }
    }
}