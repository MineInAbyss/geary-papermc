package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.prefabs.configuration.components.Action
import com.mineinabyss.geary.prefabs.configuration.components.RoleContext
import com.mineinabyss.geary.prefabs.configuration.components.Expression
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
class ExplodeAction(
    val breakBlocks: Expression<Boolean>,
    val setFire: Expression<Boolean>,
    val power: Expression<Float>,
    val at: Expression<@Contextual Location>,
) : Action {
    override fun RoleContext.execute() {
        eval(at).createExplosion(eval(power), eval(setFire), eval(breakBlocks))
    }
}

//class TemplatingYamlFormat(
//    val yaml: Yaml,
//    override val serializersModule: SerializersModule
//): StringFormat {
//    override fun <T> decodeFromString(
//        deserializer: DeserializationStrategy<T>,
//        string: String,
//    ): T {
//        val rootNode = yaml.parseToYamlNode(string)
//        yaml.decodeFromYamlNode(deserializer, rootNode)
//        val input = YamlInput.createFor(rootNode, yaml, serializersModule, yaml.configuration, deserializer.descriptor)
//        return input.decodeSerializableValue(deserializer)
//    }
//
//    public fun <T> decodeFromSource(
//        deserializer: DeserializationStrategy<T>,
//        source: Source,
//    ): T {
//    }
//
//
//    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
//        TODO("Not yet implemented")
//    }
//}
