package receiver

import collection.*
import handlers.ReadFileHandler
import java.util.*

/**
 * Singleton class, store for all study groups
 * @property stdGroupCollection Collection of [StudyGroup] (Main storage)
 */
object Receiver {
    private val stdGroupCollection: TreeMap<Long, StudyGroup> = TreeMap()

    /**
     * Load studyGroups from file via [ReadFileHandler.handle]
     * @param filename - path to file
     */
    fun loadFromFile(filename: String) {
        val fileDataReader = ReadFileHandler()
        fileDataReader.handle(filename, "")?.forEach { group ->
            if (group != null) {
                stdGroupCollection[group.getId()] = group
            }
        }
        CollectionInfo.updateElementsCount()
        println("file was loaded successfully")
    }

    /**
     * Get all stored study groups
     */
    fun getStudyGroups(): TreeMap<Long, StudyGroup> {
        return stdGroupCollection
    }

    /**
     * Get [StudyGroup] by key (id)
     */
    fun getStudyGroup(id: Long): StudyGroup? {
        return stdGroupCollection[id]
    }

    /**
     * Add new [StudyGroup] to [stdGroupCollection]
     * @param key Id of new study group
     * @param studyGroup new [StudyGroup]
     */
    fun addStudyGroup(key: Long, studyGroup: StudyGroup) {
        stdGroupCollection[key] = studyGroup
        CollectionInfo.updateElementsCount()
    }

    /**
     * Remove [StudyGroup] from [stdGroupCollection] by its key (id)
     */
    fun removeStudyGroup(key: Long) {
        stdGroupCollection.remove(key)
        CollectionInfo.updateElementsCount()
    }
}