package handlers

import collection.StudyGroup
import org.jetbrains.annotations.Nullable
import parsers.OutputParser
import receiver.Receiver
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Writes data into a file
 */
class WriteFileHandler: Handler<TreeMap<Long, StudyGroup>> {
    /**
     * @param option name of file, in that collection will be saved
     */
    override fun handle(data: TreeMap<Long, StudyGroup>, option: String) {
        try {
            val groups = Receiver.getStudyGroups()
            val writer = BufferedOutputStream(FileOutputStream("src/main/resources/$option"))
            val outputParser = OutputParser()
            val groupsData = outputParser.generateGroupsData(groups)
            val res = outputParser.parse(groupsData)
            val bytes = res.toByteArray()
            writer.write(bytes)
            writer.flush()
        } catch (e: IOException) {
            print(e.message)
        }
    }
}