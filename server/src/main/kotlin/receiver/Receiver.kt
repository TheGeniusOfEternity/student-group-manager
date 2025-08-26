package receiver

import collection.*
import dao.StudyGroupDao
import dao.UserDao
import handlers.DatabaseHandler
import java.util.*

/**
 * Singleton class, store for all study groups
 * @property stdGroupCollection Collection of [StudyGroup] (Main storage)
 */
object Receiver {
    private val stdGroupCollection: TreeMap<Long, StudyGroup> = TreeMap()
    private val usersCollection: TreeMap<Int, User> = TreeMap()

    /**
     * Load studyGroups from file via [DatabaseHandler]
     */
    fun loadFromDatabase() {
        if (DatabaseHandler.connection != null) {
            stdGroupCollection.clear()
            usersCollection.clear()
            val users = UserDao.getAll()
            users.forEach { user ->
                usersCollection[user.id!!] = user
            }

            val groups = StudyGroupDao.getAll()
            groups.forEach { studyGroup ->
                stdGroupCollection[studyGroup.getId()] = studyGroup
            }
            CollectionInfo.updateElementsCount()
        }
    }

    /**
     * Get all stored study groups
     */
    fun getStudyGroups(): TreeMap<Long, StudyGroup> {
        return stdGroupCollection
    }

    /**
     * Get all stored users
     */
    fun getUsers(): TreeMap<Int, User> {
        return usersCollection
    }

    /**
     * Get [StudyGroup] by key (id)
     */
    fun getStudyGroup(id: Long): StudyGroup? {
        return stdGroupCollection[id]
    }

    /**
     * Get [User] by key (id)
     */
    fun getUser(name: String?): User? {
        return usersCollection.values.find { user -> user.username == name }
    }

    /**
     * Clear [stdGroupCollection]
     */
    fun clearStudyGroups() {
        stdGroupCollection.clear()
        CollectionInfo.updateElementsCount()
    }
}