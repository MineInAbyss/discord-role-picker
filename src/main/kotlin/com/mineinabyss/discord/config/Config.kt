package com.mineinabyss.discord.config

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
class Config(
    val infoText: String? = null,
    val rolesMessageReference: MessageRef,
    val groups: List<RoleGroup>,
) {
    fun findByName(id: String): RoleGroup? {
        return groups.find { it.id == id }
    }
}

@Serializable
data class MessageRef(
    val channel: Snowflake,
    val message: Snowflake,
)
