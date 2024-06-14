package com.mineinabyss.discord.bot

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.inputStream
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.mineinabyss.discord.config.Config
import kotlinx.coroutines.runBlocking

class StartCommand : CliktCommand() {
    init {
        context { autoEnvvarPrefix = "BOT" }
    }

    val token by option().required()
    val config by option().inputStream().required()

    override fun run() = runBlocking {
        val config = Yaml.default.decodeFromStream<Config>(config)
        val bot = ExtensibleBot(token) {
            applicationCommands {
                enabled = true
            }
            chatCommands {
                enabled = true
                defaultPrefix = "!"
            }
            extensions {
                add { CreateButtons(config) }
            }
        }
        bot.start()
    }
}
