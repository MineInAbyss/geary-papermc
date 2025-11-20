package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.idofront.serialization.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.bukkit.Material
import team.unnamed.creative.item.tint.TintSource

@Serializable
@SerialName("geary:resourcepack")
data class ResourcePackContent(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val baseMaterial: @Serializable(MaterialByNameSerializer::class) Material? = null,
    val itemModel: @Serializable(KeySerializer::class) Key? = null,
    val model: @Serializable(KeySerializer::class) Key? = null,
    val parentModel: @Serializable(KeySerializer::class) Key = Key.key("minecraft:item/generated"),
    val textures: @Serializable(ModelTexturesSerializer::class) ModelTexturesSurrogate = ModelTexturesSurrogate(),
    val tintSources: List<@Serializable(TintSourceSerializer::class) TintSource> = listOf(),
    val itemPredicates: ItemPredicates = ItemPredicates(customModelData = null),
) {

    init {
        require(itemModel != null || model != null || textures.layers.isNotEmpty() || textures.variables.isNotEmpty()) { "ResourcePackContent must contain atleast an itemModel, model or texture reference" }
    }

    @Serializable
    data class ItemPredicates(
        @EncodeDefault(EncodeDefault.Mode.NEVER) val customModelData: Int? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val blockingModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val blockingTexture: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val brokenModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val brokenTexture: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val castModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val castTexture: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val chargedModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val chargedTexture: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val fireworkModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val fireworkTexture: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val lefthandedModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val lefthandedTexture: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val throwingModel: @Serializable(KeySerializer::class) Key? = null,
        @EncodeDefault(EncodeDefault.Mode.NEVER) val throwingTexture: @Serializable(KeySerializer::class) Key? = null,

        @EncodeDefault(EncodeDefault.Mode.NEVER) val angleModels: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val angleTextures: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val cooldownModels: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val cooldownTextures: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val damageModels: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val damageTextures: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val pullingModels: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val pullingTextures: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val timeModels: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
        @EncodeDefault(EncodeDefault.Mode.NEVER) val timeTextures: Map<@Serializable(KeySerializer::class) Key, Float> = emptyMap(),
    )
}
