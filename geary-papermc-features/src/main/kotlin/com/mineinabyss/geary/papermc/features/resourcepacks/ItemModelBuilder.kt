package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.idofront.serialization.KeySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures

@Serializable
@SerialName("geary:item_model")
data class ItemModelBuilder(
    val model: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition,
    val key: @Serializable(KeySerializer::class) Key? = null,
)

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
