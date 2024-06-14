package com.mineinabyss.discord.bot

import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.components.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.ackPublic
import com.kotlindiscord.kord.extensions.utils.hasRole
import com.mineinabyss.discord.config.Config
import com.mineinabyss.discord.config.RoleGroup
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CreateButtons(
    val config: Config,
) : Extension() {
    override val name = "ping"
    private val mutex = Mutex()
    private val pastResponses = mutableMapOf<Snowflake, MessageInteractionResponseBehavior>()

    suspend fun updateRolesMessage() {
        val message = Message(
            kord.rest.channel.getMessage(
                config.rolesMessageReference.channel,
                config.rolesMessageReference.message
            ).toData(), kord
        )
        message.edit {
            content = config.infoText
            components {
                config.groups.forEach {
                    publicButton {
                        label = it.name
                        style = it.style
                        id = "roles/${it.id}"

                        // Never want to run in this context, only the event listener
                        check { passed = false }
                        action { }
                    }
                }
            }
        }
    }

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "rolesmessage"
            description = "Create roles message"
            check {
                hasPermission(Permission.Administrator)
            }
            action {
                channel.createMessage {
                    content = "Place this message's id into the config"
                }
                respond { content = "Created roles message" }
            }
        }
        ephemeralSlashCommand {
            name = "rolesupdate"
            description = "Updates role message based on config"
            check {
                hasPermission(Permission.Administrator)
            }
            action {
                updateRolesMessage()
                respond { content = "Updated roles message" }
            }
        }
        event<SelectMenuInteractionCreateEvent> {
            action {
                val comp = event.interaction.componentId
                println("Interact $comp")
                when {
                    comp.startsWith("select/") -> {
                        event.interaction.channel.createMessage {
                            val options =
                                config.findByName(comp.removePrefix("select/"))?.options ?: return@createMessage
                            val member = memberFor(event) ?: return@createMessage
                            val selectedRoles = event.interaction.values.map { Snowflake(it) }.toSet()
                            val (add, remove) = options.partition {
                                it.role in selectedRoles
                            }
                            add.forEach { member.addRole(it.role!!) }
                            remove.forEach {
                                member.removeRole(it.role!!)
                            }
                            event.interaction.ackPublic(deferred = true)
                        }
                    }
                }
            }
        }
        event<ButtonInteractionCreateEvent> {
            action {
                val comp = event.interaction.componentId
                println("Interact $comp")
                when {
                    comp.startsWith("roles/") -> {
                        val response = event.interaction.respondEphemeral {
                            mutex.withLock {
                                pastResponses[event.interaction.user.id]?.delete()
                            }
                            components {
                                createComponents(
                                    event, config.findByName(comp.removePrefix("roles/")) ?: return@components
                                )
                            }
                        }
                        mutex.withLock {
                            pastResponses[event.interaction.user.id] = response
                        }
                    }
                }
            }
        }
        updateRolesMessage()
    }

    suspend inline fun ComponentContainer.createComponents(
        event: ButtonInteractionCreateEvent,
        group: RoleGroup,
    ) {
        publicStringSelectMenu {
            val guild = guildFor(event) ?: return@publicStringSelectMenu
            val member = event.interaction.user.asMember(guild.id)
            id = "select/${group.id}"
            group.options.forEach { option ->
                val role = option.role?.let { it1 -> guild.getRole(it1) }
                option(option.name ?: role?.name ?: "", role?.id?.toString() ?: option.name!!) {
                    emoji = option.emoji
                    description = option.desc
                    if (role != null)
                        default = member.hasRole(role)
                }
            }

            minimumChoices = 0
            maximumChoices = 25

            action {
            }
        }
    }
}

