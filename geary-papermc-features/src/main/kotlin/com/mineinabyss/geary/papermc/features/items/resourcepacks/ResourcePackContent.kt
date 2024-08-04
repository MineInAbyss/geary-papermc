package com.mineinabyss.geary.papermc.features.items.resourcepacks

import com.mineinabyss.idofront.serialization.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.bukkit.Material
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.ItemPredicate

@Serializable
@SerialName("geary:resourcepack")
data class ResourcePackContent(
    val baseMaterial: @Serializable(MaterialByNameSerializer::class) Material,
    val model: @Serializable(KeySerializer::class) Key? = null,
    val parentModel: @Serializable(KeySerializer::class) Key = Key.key("minecraft:item/generated"),
    val textures: @Serializable(ModelTexturesSerializer::class) ModelTexturesSurrogate = ModelTexturesSurrogate(),
    val itemPredicates: ItemPredicates
) {

    init {
        require(model != null || textures.layers.isNotEmpty() || textures.variables.isNotEmpty()) { "ResourcePackContent must contain atleast a model or texture reference" }
    }

    fun itemOverrides(modelKey: Key): List<ItemOverride> {
        val overrides = mutableListOf<ItemOverride>()

        // Shields
        (itemPredicates.blockingModel ?: itemPredicates.blockingTexture?.let { modelKey.plus("_blocking") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.blocking()))
        }
        // Elytras
        (itemPredicates.brokenModel ?: itemPredicates.brokenTexture?.let { modelKey.plus("_broken") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.broken()))
        }
        // Fishing Rods
        overrides.add(ItemOverride.of(modelKey, itemPredicates.customModelData()))
        (itemPredicates.castModel ?: itemPredicates.castTexture?.let { modelKey.plus("_cast") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.cast()))
        }
        // Charged Crossbow
        (itemPredicates.chargedModel ?: itemPredicates.chargedTexture?.let { modelKey.plus("_charged") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.charged()))
        }
        // Charged Crossbow with Firework
        (itemPredicates.fireworkModel ?: itemPredicates.fireworkTexture?.let { modelKey.plus("_firework") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.firework()))
        }
        // Lefthanded-players
        (itemPredicates.lefthandedModel ?: itemPredicates.lefthandedTexture?.let { modelKey.plus("_lefthanded") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.lefthanded()))
        }
        // Tridents
        (itemPredicates.throwingModel ?: itemPredicates.throwingTexture?.let { modelKey.plus("_throwing") })?.let {
            overrides.add(ItemOverride.of(it, itemPredicates.customModelData(), ItemPredicate.throwing()))
        }

        fun Map<Key, Float>.predicateModel(suffix: String, action: (Map.Entry<Key, Float>) -> ItemOverride) = this.toList().mapIndexed { index, (_, damage) -> modelKey.plus("_${suffix}_$index") to damage.coerceIn(0f..1f) }.toMap().map(action).forEach(overrides::add)
        // Compasses
        itemPredicates.angleModels.takeUnless { it.isEmpty() } ?: itemPredicates.angleTextures.predicateModel("angle") { (key, angle) ->
            ItemOverride.of(key, itemPredicates.customModelData(), ItemPredicate.angle(angle))
        }
        // Cooldown remaining on an item
        itemPredicates.cooldownModels.takeUnless { it.isEmpty() } ?: itemPredicates.cooldownTextures.predicateModel("cooldown") { (key, damage) ->
            ItemOverride.of(key, itemPredicates.customModelData(), ItemPredicate.damaged(), ItemPredicate.cooldown(damage))
        }
        // Durability of an item
        itemPredicates.damageModels.takeUnless { it.isEmpty() } ?: itemPredicates.damageTextures.predicateModel("damage") { (key, damage) ->
            ItemOverride.of(key, itemPredicates.customModelData(), ItemPredicate.damaged(), ItemPredicate.damage(damage))
        }
        // Bows & Crossbows
        itemPredicates.pullingModels.takeUnless { it.isEmpty() } ?: itemPredicates.pullingTextures.predicateModel("pulling") { (key, pulling) ->
            ItemOverride.of(key, itemPredicates.customModelData(), ItemPredicate.pulling(), ItemPredicate.pull(pulling))
        }
        // Clocks
        itemPredicates.timeModels.takeUnless { it.isEmpty() } ?: itemPredicates.timeTextures.predicateModel("time") { (key, time) ->
            ItemOverride.of(key, itemPredicates.customModelData(), ItemPredicate.time(time))
        }

        return overrides
    }

    private fun Key.plus(suffix: String) = Key.key(namespace(), value().plus(suffix))

    @Serializable
    data class ItemPredicates(
        val customModelData: Int,
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
    ) {
        fun customModelData(): ItemPredicate = ItemPredicate.customModelData(customModelData)
    }
}
