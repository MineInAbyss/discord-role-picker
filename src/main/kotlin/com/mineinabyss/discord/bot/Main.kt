package com.mineinabyss.discord.bot

import com.mineinabyss.discord.config.RoleGroup
import com.mineinabyss.discord.config.RoleOption
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import kotlinx.coroutines.sync.Mutex

fun main(args: Array<String>) = StartCommand().main(args)
