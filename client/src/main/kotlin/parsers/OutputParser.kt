package parsers

import GroupData
import Property
import annotations.Pos
import collection.StudyGroup
import receiver.Receiver
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Parses [Receiver.stdGroupCollection] in file
 */
object OutputParser: Parser<ArrayList<GroupData>> {
    /**
     * Convert [GroupData] into row, then concatenate row into one
     * @param data ArrayList of [GroupData]
     * @return String representation for files of [Receiver.stdGroupCollection]
     */
    override fun parse(data: ArrayList<GroupData>): String {
        var result = ""
        data.forEach {groupData ->
            var row = ""
            groupData.forEach {property ->
                if (property.first != "creationDate") {
                    row += if (property.second == "null") {
                        ";"
                    } else {
                        "${property.second};"
                    }
                }
            }
            result = result + row.dropLast(1) + "\n"
        }
        return result
    }

    /**
     * Get [GroupData] from single object, can work recursive
     * @param obj Object needed to be parsed
     * @param groupData [GroupData] with parsed [Property]s
     * @return The same [GroupData], that was given
     */
    private fun parseObject(obj: Any, groupData: GroupData): GroupData {
        val properties = obj.javaClass.kotlin.declaredMemberProperties.map { property ->
            val order = property.annotations.find { it is Pos } as? Pos
            property to order?.order
        }.sortedBy { it.second }

        properties.forEach { (property, _) ->
            property.isAccessible = true
            if ((property.get(obj) != null) && (property.name == "coordinates" || property.name == "groupAdmin")) {
                parseObject(property.get(obj)!!, groupData)
            } else {
                if (property.name == "birthday") {
                    groupData.add(Property(property.name, SimpleDateFormat("dd.MM.yyyy").format(property.get(obj)!!)))
                } else if (property.returnType == String::class.createType()) {
                    groupData.add(Property(property.name, "'${property.get(obj).toString()}'"))
                } else {
                    groupData.add(Property(property.name, property.get(obj).toString()))
                }
            }

        }
        return groupData
    }

    /**
     * Convert [Receiver.stdGroupCollection] into ArrayList of [GroupData]
     * @param data Study groups [Receiver.stdGroupCollection]
     * @return ArrayList of study groups in [GroupData]
     */
    fun generateGroupsData(data: TreeMap<Long, StudyGroup>): ArrayList<GroupData> {
        val groupsData = ArrayList<GroupData>()
        data.forEach { (_, group) ->
            var groupData = GroupData()
            groupData = parseObject(group, groupData)
            groupsData.add(groupData)
        }
        return groupsData
    }
}