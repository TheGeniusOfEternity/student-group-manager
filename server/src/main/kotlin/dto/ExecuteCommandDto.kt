package dto

import kotlinx.serialization.Serializable
import collection.StudyGroup
import kotlinx.serialization.SerialName

/**
 * Union type for [ExecuteCommandDto.params] property
 */
@Serializable
sealed class CommandParam  {
    @Serializable
    @SerialName("long")
    data class LongParam(val value: Long?) : CommandParam()

    @Serializable
    @SerialName("studyGroup")
    data class StudyGroupParam(val value: StudyGroup?) : CommandParam()
}

/**
 * ExecuteCommandDto Dto - for requesting command execution
 * @property name - Command's name
 * @property params - [CommandParam] additional params of command. Could be [Long] as id, [StudyGroup] as new study group, or simply null
 */
@Serializable
data class ExecuteCommandDto(
    val name: String,
    val params: CommandParam?
)
