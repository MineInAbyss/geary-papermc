package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.components.ComponentInfo
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@Serializable(with = SetTarget.Serializer::class)
class SetTarget(val findByKind: ComponentId) {
    class Serializer : KSerializer<SetTarget> {
        override val descriptor = PrimitiveSerialDescriptor("geary:set.target", PrimitiveKind.STRING)

        private val polymorphicListAsMapSerializer = PolymorphicListAsMapSerializer.ofComponents()
        override fun deserialize(decoder: Decoder): SetTarget {
            val namespaces = polymorphicListAsMapSerializer.getNamespaces(decoder.serializersModule)
            val kind = decoder.decodeString()
            val kClass = serializableComponents.serializers.getClassFor(kind, namespaces)
            return SetTarget(componentId(kClass))
        }

        override fun serialize(encoder: Encoder, value: SetTarget) {
            val kClass = value.findByKind.toGeary().get<ComponentInfo>()?.kClass
                    as? KClass<*>
                ?: error("Failed to find class for ${value.findByKind}")

            val serialName = serializableComponents.serializers.getSerialNameFor(kClass)
                ?: error("Failed to find serial name for $kClass")
            encoder.encodeString(serialName)
        }
    }
}
