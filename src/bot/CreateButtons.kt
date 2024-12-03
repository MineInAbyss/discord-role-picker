package bot

import config.Config
import config.RoleGroup
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.actionRow
import java.util.concurrent.ConcurrentHashMap

class CreateButtons(
    val kord: Kord,
    val config: Config,
) {
    private val pastResponses = ConcurrentHashMap<Snowflake, MessageInteractionResponseBehavior>()

    suspend fun updateRolesMessage() {
        if (config.rolesMessageReference == null) return
        val message = Message(
            kord.rest.channel.getMessage(
                config.rolesMessageReference.channel,
                config.rolesMessageReference.message
            ).toData(), kord
        )
        message.edit {
            actionRow {
                config.groups.forEach {
                    interactionButton(it.style, "roles/${it.id}") {
                        label = it.name
                    }
                }
            }
        }
    }

    suspend fun init() {
        val roleMessageCommand =
            kord.createGuildChatInputCommand(config.guildId, "rolesmessage", "Create roles message") {
                defaultMemberPermissions = Permissions(Permission.Administrator)
            }

        val rolesUpdateCommand =
            kord.createGuildChatInputCommand(config.guildId, "rolesupdate", "Update roles message") {
                defaultMemberPermissions = Permissions(Permission.Administrator)
            }

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            println(interaction)
            when (interaction.invokedCommandId) {
                roleMessageCommand.id -> {
                    val response = interaction.deferEphemeralResponse()
                    interaction.channel.createMessage("Place this message's id into the config")
                    response.respond {
                        content = "Created roles message"
                    }
                }

                rolesUpdateCommand.id -> {
                    val response = interaction.deferEphemeralResponse()
                    updateRolesMessage()
                    response.respond { content = "Updated roles message" }
                }
            }
        }

        kord.on<SelectMenuInteractionCreateEvent> {
            val comp = interaction.componentId
            when {
                comp.startsWith("select/") -> {
                    val response = interaction.deferEphemeralMessageUpdate()
                    val options = config.findByName(comp.removePrefix("select/"))?.options ?: return@on
                    val guild = kord.getGuild(config.guildId)
                    val member = guild.getMember(interaction.user.id)
                    val selectedRoles = interaction.values.filter { it != "unknown" }.map { Snowflake(it) }.toSet()
                    val (add, remove) = options.partition {
                        it.role in selectedRoles
                    }
                    add.forEach { member.addRole(it.role ?: return@forEach) }
                    remove.forEach { member.removeRole(it.role ?: return@forEach) }
                    response.edit {}
                }
            }
        }

        kord.on<ButtonInteractionCreateEvent> {
            val comp = interaction.componentId
            when {
                comp.startsWith("roles/") -> {
                    val response = interaction.respondEphemeral {
                        pastResponses[interaction.user.id]?.delete()
                        actionRow {
                            createComponents(
                                this@on, config.findByName(comp.removePrefix("roles/")) ?: return@actionRow
                            )
                        }
                    }
                    pastResponses[interaction.user.id] = response
                }
            }
        }
        updateRolesMessage()
    }

    suspend inline fun ActionRowBuilder.createComponents(
        event: ButtonInteractionCreateEvent,
        group: RoleGroup,
    ) = stringSelect("select/${group.id}") {
        val guild = kord.getGuild(config.guildId)
        val member = guild.getMember(event.interaction.user.id)
        val optionCount = group.options.count { option ->
            val role = option.role?.let { guild.getRole(it) }
            option(option.name ?: role?.name ?: "", role?.id?.toString() ?: return@count false) {
                emoji = option.emoji
                description = option.desc
                default = member.roleIds.contains(role.id)
            }
            true
        }
        allowedValues = 0..optionCount
    }
}

