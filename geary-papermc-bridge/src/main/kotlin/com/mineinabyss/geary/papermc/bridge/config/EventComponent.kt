package com.mineinabyss.geary.papermc.bridge.config

import com.charleskorn.kaml.YamlInput
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with=EventComponent.Serializer::class)
class EventComponent(val type: ComponentId, val data: GearyComponent?) {
    class Serializer: KSerializer<EventComponent> {
        @OptIn(InternalSerializationApi::class)
        override val descriptor: SerialDescriptor =
            buildSerialDescriptor("geary:event", SerialKind.CONTEXTUAL)

        private val polymorphicListAsMapSerializer = PolymorphicListAsMapSerializer.ofComponents()

        override fun deserialize(decoder: Decoder): EventComponent {
            val composite = runCatching { (decoder as YamlInput).beginStructure(String.serializer().descriptor) }.getOrNull()
            if (composite != null) {
                val type = composite.decodeStringElement(String.serializer().descriptor, 0)
                composite.endStructure(String.serializer().descriptor)
                val namespaces = polymorphicListAsMapSerializer.getNamespaces(decoder.serializersModule)
                val typeComponentId = componentId(serializableComponents.serializers.getClassFor(type, namespaces))
                return EventComponent(typeComponentId, null)
            }
            val components = polymorphicListAsMapSerializer.deserialize(decoder)
            val comp = components.single()

            return EventComponent(componentId(comp::class), comp)
        }

        override fun serialize(encoder: Encoder, value: EventComponent) {
            TODO("Not yet implemented")
        }
    }
}
