package com.mineinabyss.geary.papermc.features.resourcepacks

import com.google.gson.JsonObject
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures

@Serializable(ItemModelBuilder.Serializer::class)
data class ItemModelBuilder(
    val model: ItemModelDefinition,
    val key: Key? = null,
) {
    // The model may sit under `model` or inline at the root, a root holding only `model` reads the same either way
    object Serializer : InnerSerializer<ItemModelDefinition, ItemModelBuilder>(
        serialName = "geary:item_model",
        inner = ItemModelDefinitionSerializer,
        transform = { definition ->
            val root = definition.json.takeIf { it.isJsonObject }?.asJsonObject?.deepCopy()
            val key = root?.remove("key")?.asString?.let(Key::key)
            val model = root?.takeIf { it.keySet() == setOf("model") }?.get("model") ?: root ?: definition.json
            ItemModelBuilder(ItemModelDefinition(model), key)
        },
        inverseTransform = { builder ->
            ItemModelDefinition(JsonObject().apply {
                add("model", builder.model.json)
                builder.key?.let { addProperty("key", it.asString()) }
            })
        },
    )
}

/** Generates the models an [ItemModelDefinition] asks for, off the textures and .bbmodels an artist provides */
class ModelBuilder(
    private val pack: ResourcePack,
    format: PackFormat,
) {
    private val bbModels = BBModelGenerator(pack, format)

    /** Flat models are written at their texture's own path, the way prefabs already name the two in step */
    fun fromTexture(texture: Key, parentModel: Key): Key {
        Model.model()
            .key(texture)
            .parent(parentModel)
            .textures(ModelTextures.of(listOf(ModelTexture.ofKey(texture)), null, emptyMap()))
            .build().addTo(pack)
        return texture
    }

    fun fromBBModel(bbmodel: Key): Key = bbModels.generate(bbmodel)
}
