package com.mineinabyss.geary.papermc.features.resourcepacks

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.serialize.minecraft.item.ItemSerializer

@Serializable(ItemModelDefinitionSerializer::class)
class ItemModelDefinition(val json: JsonElement)

object ItemModelDefinitionSerializer : KSerializer<ItemModelDefinition> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): ItemModelDefinition {
        val input = decoder as? YamlInput ?: return ItemModelDefinition(JsonParser.parseString(decoder.decodeString()))
        return ItemModelDefinition(input.node.toJson())
    }

    override fun serialize(encoder: Encoder, value: ItemModelDefinition) = encoder.encodeString(value.json.toString())

    // Select cases are matched against strings, so `when: 1` has to stay "1" while creative reads colors and thresholds only from JSON numbers
    private fun YamlNode.toJson(asStrings: Boolean = false): JsonElement = when (this) {
        is YamlMap -> JsonObject().also { json ->
            entries.forEach { (key, value) -> json.add(key.content.camelToSnake(), value.toJson(asStrings || key.content == "when")) }
        }
        is YamlList -> JsonArray().also { json -> items.forEach { json.add(it.toJson(asStrings)) } }
        is YamlScalar -> if (asStrings) JsonPrimitive(content) else scalar(content)
        is YamlTaggedNode -> innerNode.toJson(asStrings)
        is YamlNull -> JsonNull.INSTANCE
    }

    private fun scalar(content: String): JsonPrimitive =
        content.toLongOrNull()?.let(::JsonPrimitive)
            ?: content.toDoubleOrNull()?.let(::JsonPrimitive)
            ?: content.toBooleanStrictOrNull()?.let(::JsonPrimitive)
            ?: JsonPrimitive(content)

    private fun String.camelToSnake() = replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()
}

class ItemModelResolver(
    private val builder: ModelBuilder,
    private val fallbackModel: Key,
) {
    fun resolve(definition: ItemModelDefinition): ItemModel =
        ItemSerializer.INSTANCE.deserializeItemModel(expand(definition.json.deepCopy()))

    private fun expand(model: JsonElement): JsonElement {
        if (model.isJsonPrimitive) return reference(Key.key(model.asString), null)
        val node = model.asJsonObject

        val type = node.get("type")?.asString ?: when {
            node.has("texture") -> "texture"
            node.has("bbmodel") -> "bbmodel"
            else -> "reference"
        }
        when (type) {
            "texture" -> return reference(builder.fromTexture(node.key("texture")!!, node.key("parent_model") ?: Key.key("minecraft:item/generated")), node.get("tints"))
            "bbmodel" -> return reference(builder.fromBBModel(node.key("bbmodel")!!), node.get("tints"))
            "reference" -> return reference(node.key("model") ?: fallbackModel, node.get("tints"))
        }

        MODEL_FIELDS.filter { node.get(it)?.isJsonNull == true }.forEach(node::remove)
        MODEL_FIELDS.filter(node::has).forEach { node.add(it, expand(node.get(it))) }
        node.get("models")?.asJsonArray?.let { models -> models.forEachIndexed { i, it -> models.set(i, expand(it)) } }
        ENTRY_LISTS.mapNotNull { node.get(it)?.asJsonArray }.flatten().map(JsonElement::getAsJsonObject).forEach { entry ->
            entry.get("model")?.let { entry.add("model", expand(it)) }
        }
        return node
    }

    private fun reference(model: Key, tints: JsonElement?) = JsonObject().apply {
        addProperty("type", "minecraft:model")
        addProperty("model", model.asString())
        tints?.let { add("tints", it) }
    }

    private fun JsonObject.key(name: String): Key? = get(name)?.asString?.let(Key::key)

    private companion object {
        val MODEL_FIELDS = listOf("on_true", "on_false", "fallback")
        val ENTRY_LISTS = listOf("cases", "entries")
    }
}
