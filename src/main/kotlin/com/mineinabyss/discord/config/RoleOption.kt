package com.mineinabyss.discord.config

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class RoleOption(
    val role: Snowflake? = null,
    val emoji: DiscordPartialEmoji? = null,
    val name: String? = null,
    val desc: String? = null,
)
