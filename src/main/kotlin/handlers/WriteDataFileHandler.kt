package handlers

import collection.StudyGroup
import parsers.OutputParser
import receiver.Receiver
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Writes data into a file
 */
class WriteDataFileHandler: Handler<TreeMap<Long, StudyGroup>, String> {
    /**
     * @param option name of file, in that collection will be saved
     */
    override fun handle(data: TreeMap<Long, StudyGroup>, option: String) {
        try {
            val groups = Receiver.getStudyGroups()
            val writer = BufferedOutputStream(FileOutputStream("data/$option"))
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