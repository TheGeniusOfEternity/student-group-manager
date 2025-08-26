package validators

import core.GroupData
import collection.*
import handlers.IOHandler
import java.text.ParseException
import java.text.SimpleDateFormat

class GroupDataValidator : Validator<GroupData> {
    /**
     * Check [GroupData] validation & convert it into new [StudyGroup]
     * @param data [GroupData], needed to check and convert
     * @return new [StudyGroup]
     */
    override fun validateData(data: GroupData): StudyGroup? {
        try {
            val propertyValidator = PropertyValidator()
            val formatter = SimpleDateFormat("yyyy-mm-dd")
            data.forEach { pair ->
                val (property, value) = pair
                if (!propertyValidator.validateData(Pair(property, value))) return null
            }
            val groupAdmin: Person? = if (data.size != 11) null else Person(
                data[8].second!!,
                try {
                    formatter.parse(data[9].second)
                } catch (e: ParseException) { null },
                if (data[10].second.isNullOrEmpty()) null else Country.valueOf(data[10].second!!.uppercase())
            )

            return StudyGroup(
                id = data[0].second!!.toLong(),
                name = data[1].second!!,
                coordinates = Coordinates(data[2].second!!.toInt(), data[3].second!!.toLong()),
                studentsCount = data[4].second!!.toInt(),
                transferredStudents = data[5].second?.toLongOrNull(),
                averageMark = data[6].second?.toIntOrNull(),
                formOfEducation = if (data[7].second.isNullOrEmpty()) null else FormOfEducation.valueOf(data[7].second!!.uppercase()),
                groupAdmin = groupAdmin,
                ownerName = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            IOHandler.responsesThreads.add("Group #${data[0].second} validation error: incorrect GroupData format (size is ${data.size})")
            return null
        }
    }
}