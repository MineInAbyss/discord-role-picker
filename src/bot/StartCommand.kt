package bot

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.inputStream
import config.Config
import dev.kord.cache.map.MapLikeCollection
import dev.kord.cache.map.internal.MapEntryCache
import dev.kord.common.entity.DiscordChatComponent
import dev.kord.common.entity.DiscordComponent
import dev.kord.core.Kord
import dev.kord.rest.ratelimit.ExclusionRequestRateLimiter
import dev.kord.rest.request.KtorRequestHandler
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val polymorphismFix = Json {
    encodeDefaults = false
    allowStructuredMapKeys = true
    ignoreUnknownKeys = true
    isLenient = true
    classDiscriminator = "_type"
    serializersModule = SerializersModule {
        polymorphic(DiscordComponent::class) {
            subclass(DiscordChatComponent.serializer())
        }
    }
}

class StartCommand : CliktCommand() {
    init {
        context { autoEnvvarPrefix = "BOT" }
    }

    val token by option().required()
    val config by option().inputStream().required()

    override fun run() = runBlocking {
        val config = Yaml.default.decodeFromStream<Config>(config)
        val kord = Kord(token) {
            cache {
                guilds { cache, description ->
                    MapEntryCache(cache, description, MapLikeCollection.concurrentHashMap())
                }
                roles { cache, description ->
                    MapEntryCache(cache, description, MapLikeCollection.concurrentHashMap())
                }
                emojis { cache, description ->
                    MapEntryCache(cache, description, MapLikeCollection.concurrentHashMap())
                }
            }
            requestHandler {
                KtorRequestHandler(
                    it.httpClient,
                    ExclusionRequestRateLimiter(),
                    token = token,
                    parser = polymorphismFix
                )
            }
        }
        CreateButtons(kord, config).init()
        kord.login()
    }
}
