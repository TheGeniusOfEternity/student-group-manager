package dto

import kotlinx.serialization.Serializable


/*
    Dto with command info from server to client
 */
@Serializable
data class CommandInfoDto(val name: String, val description: String, val paramTypeName: String?)