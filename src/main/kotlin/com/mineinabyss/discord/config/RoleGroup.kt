package com.mineinabyss.discord.config

import dev.kord.common.entity.ButtonStyle
import kotlinx.serialization.Serializable

@Serializable
data class RoleGroup(
    val id: String,
    val name: String,
    @Serializable(with = ButtonStyleSerializer::class)
    val style: ButtonStyle,
    val options: List<RoleOption>,
)
