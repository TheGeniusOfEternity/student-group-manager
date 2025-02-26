package receiver

import collection.*
import java.io.FileReader
import java.io.IOException
import java.util.*

object Receiver {
    private val stdGroupCollection: TreeMap<Long, StudyGroup> = TreeMap()
    private val collectionInfo: CollectionInfo = CollectionInfo()

    fun readFileData(fileName: String) {
        try {
            val fileReader = FileReader(fileName)
            val validator = Validator()
            var index: Int = fileReader.read()
            val currentGroupData: ArrayList<String> = ArrayList()
            var currentField = ""
            while (index != -1) {
                when (val c: Char = index.toChar()) {
                    ';' -> {
                        currentGroupData.add(currentField)
                        currentField = ""
                    }
                    '\n' -> {
                        currentGroupData.add(currentField)
                        currentField = ""
                        val currentGroup: StudyGroup = validator.validateGroupData(currentGroupData)
                        stdGroupCollection[currentGroup.getId()] = currentGroup
                        collectionInfo.incrementElementCount()
                        currentGroupData.clear()
                    }
                    '"' -> {
                        currentField += ""
                    }
                    else -> {
                        currentField += c
                    }
                }
                index = fileReader.read()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getStudyGroups(): TreeMap<Long, StudyGroup> {
        return stdGroupCollection
    }

    fun getCollectionInfo(): CollectionInfo {
        return collectionInfo
    }
}