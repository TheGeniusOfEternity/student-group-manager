package dto

import kotlinx.serialization.Serializable
import collection.StudyGroup
/**
 * ExecuteCommandDto Dto - for requesting command execution
 * @property name - Command's name
 * @property params - [T] additional params of command. Could be [Long] as id, [StudyGroup] as new study group
 */
@Serializable
data class ExecuteCommandDto<T>(
    val name: String,
    val params: T?
)
