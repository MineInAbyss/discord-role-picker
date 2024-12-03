package config

import dev.kord.common.entity.ButtonStyle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


internal object ButtonStyleSerializer : KSerializer<ButtonStyle> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ButtonStyle", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ButtonStyle) {
        when (value) {
            ButtonStyle.Primary -> encoder.encodeInt(1)
            ButtonStyle.Secondary -> encoder.encodeInt(2)
            ButtonStyle.Success -> encoder.encodeInt(3)
            ButtonStyle.Danger -> encoder.encodeInt(4)
            ButtonStyle.Link -> encoder.encodeInt(5)
            else -> error("Unknown ButtonStyle $value")
        }
    }

    override fun deserialize(decoder: Decoder): ButtonStyle = when (decoder.decodeString()) {
        "Primary" -> ButtonStyle.Primary
        "Secondary" -> ButtonStyle.Secondary
        "Success" -> ButtonStyle.Success
        "Danger" -> ButtonStyle.Danger
        "Link" -> ButtonStyle.Link
        else -> error("Unknown ButtonStyle")
    }
}
