package com.mineinabyss.geary.papermc.features.items.resourcepacks

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.KeySerializer
import com.mineinabyss.idofront.serialization.MaterialByNameSerializer
import com.mineinabyss.idofront.serialization.ModelTexturesSerializer
import com.mineinabyss.idofront.serialization.ModelTexturesSurrogate
import com.mineinabyss.idofront.serialization.TintSourceSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import team.unnamed.creative.item.tint.TintSource
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.ItemPredicate

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

    fun itemOverrides(modelKey: Key, prefabKey: PrefabKey, itemStack: ItemStack?): List<ItemOverride> {
        val overrides = mutableListOf<ItemOverride>()
        val cmdPredicate = itemPredicates.customModelData(itemStack) ?: run {
            Geary.w("$prefabKey has no CustomModelData specified in either ResourcePackContent or SerializableItemStack components")
            ItemPredicate.customModelData(0)
        }

        // Shields
        (itemPredicates.blockingModel ?: itemPredicates.blockingTexture?.let { modelKey.plus("_blocking") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.blocking()))
        }
        // Elytras
        (itemPredicates.brokenModel ?: itemPredicates.brokenTexture?.let { modelKey.plus("_broken") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.broken()))
        }
        // Fishing Rods
        overrides.add(ItemOverride.of(modelKey, cmdPredicate))
        (itemPredicates.castModel ?: itemPredicates.castTexture?.let { modelKey.plus("_cast") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.cast()))
        }
        // Charged Crossbow
        (itemPredicates.chargedModel ?: itemPredicates.chargedTexture?.let { modelKey.plus("_charged") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.charged()))
        }
        // Charged Crossbow with Firework
        (itemPredicates.fireworkModel ?: itemPredicates.fireworkTexture?.let { modelKey.plus("_firework") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.firework()))
        }
        // Lefthanded-players
        (itemPredicates.lefthandedModel
            ?: itemPredicates.lefthandedTexture?.let { modelKey.plus("_lefthanded") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.lefthanded()))
        }
        // Tridents
        (itemPredicates.throwingModel ?: itemPredicates.throwingTexture?.let { modelKey.plus("_throwing") })?.let {
            overrides.add(ItemOverride.of(it, cmdPredicate, ItemPredicate.throwing()))
        }

        fun Map<Key, Float>.predicateModel(suffix: String, action: (Map.Entry<Key, Float>) -> ItemOverride) =
            this.toList()
                .mapIndexed { index, (_, damage) -> modelKey.plus("_${suffix}_$index") to damage.coerceIn(0f..1f) }
                .toMap().map(action).forEach(overrides::add)
        // Compasses
        itemPredicates.angleModels.takeUnless { it.isEmpty() }
            ?: itemPredicates.angleTextures.predicateModel("angle") { (key, angle) ->
                ItemOverride.of(key, cmdPredicate, ItemPredicate.angle(angle))
            }
        // Cooldown remaining on an item
        itemPredicates.cooldownModels.takeUnless { it.isEmpty() }
            ?: itemPredicates.cooldownTextures.predicateModel("cooldown") { (key, damage) ->
                ItemOverride.of(
                    key,
                    cmdPredicate,
                    ItemPredicate.damaged(),
                    ItemPredicate.cooldown(damage)
                )
            }
        // Durability of an item
        itemPredicates.damageModels.takeUnless { it.isEmpty() }
            ?: itemPredicates.damageTextures.predicateModel("damage") { (key, damage) ->
                ItemOverride.of(
                    key,
                    cmdPredicate,
                    ItemPredicate.damaged(),
                    ItemPredicate.damage(damage)
                )
            }
        // Bows & Crossbows
        itemPredicates.pullingModels.takeUnless { it.isEmpty() }
            ?: itemPredicates.pullingTextures.predicateModel("pulling") { (key, pulling) ->
                ItemOverride.of(
                    key,
                    cmdPredicate,
                    ItemPredicate.pulling(),
                    ItemPredicate.pull(pulling)
                )
            }
        // Clocks
        itemPredicates.timeModels.takeUnless { it.isEmpty() }
            ?: itemPredicates.timeTextures.predicateModel("time") { (key, time) ->
                ItemOverride.of(key, cmdPredicate, ItemPredicate.time(time))
            }

        return overrides
    }

    private fun Key.plus(suffix: String) = Key.key(namespace(), value().plus(suffix))

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
    ) {
        fun customModelData(itemStack: ItemStack?): ItemPredicate? =
            (customModelData ?: itemStack?.itemMeta?.takeIf { it.hasCustomModelData() }?.customModelData)?.let(
                ItemPredicate::customModelData
            )
    }
}
