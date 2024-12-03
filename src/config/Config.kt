package config

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
class Config(
    val infoText: String? = null,
    val rolesMessageReference: MessageRef? = null,
    val groups: List<RoleGroup>,
    val guildId: Snowflake,
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
