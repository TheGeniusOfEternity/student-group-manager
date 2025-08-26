package core

import collection.StudyGroup
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * Singleton object for storing program state
 * @property isRunning - check if program is running or not
 * @property connectedToServer - is client connect to server, or not
 * @property host - Address to message broker (Rabbit MQ)
 * @property credentials - storage for all auth info
 */
object State {
    var isRunning = false
    @Volatile
    var connectedToServer = false
    @Volatile
    var isAuthenticated = false
    var host: String? = null
    val appName = "client-${UUID.randomUUID()}"
    val credentials: HashMap<String, String> = HashMap()
    private var openedFiles: Stack<Pair<String, Int?>> = Stack()
    val localCollection: ConcurrentHashMap<Long, StudyGroup> = ConcurrentHashMap()
    var tempGroup: StudyGroup? = null

    /**
     * Add new opened file into [openedFiles]
     */
    fun addOpenedFile(newFile: Pair<String, Int?>) {
        openedFiles.add(newFile)
    }

    /**
     * Get currently opened files
     * @return [openedFiles]
     */
    fun getOpenedFiles(): Stack<Pair<String, Int?>> {
        return openedFiles
    }

    /**
     * Returns file by its name
     * @return [Pair] of filename and index where program was paused reading
     */
    fun getFileByName(fileName: String): Pair<String, Int?>? {
        openedFiles.forEach { if (it.first == fileName) return it }
        return null
    }

    fun updateOpenedFile(filename: String, newIndex: Int) {
        openedFiles.forEach {
            if (it.first == filename) {
                this.removeOpenedFile()
                this.addOpenedFile(Pair(filename, newIndex))
            }
        }
    }

    /**
     * Clears opened filename on file's close
     */
    fun removeOpenedFile() {
        openedFiles.removeLast()
    }
}