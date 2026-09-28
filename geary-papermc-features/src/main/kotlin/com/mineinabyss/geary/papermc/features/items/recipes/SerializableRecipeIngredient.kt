package com.mineinabyss.geary.papermc.features.items.recipes

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlMap
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.SerializableItemStackSerializer
import com.mineinabyss.idofront.serialization.StringsSerializer
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

/**
 * [damage] takes durability off the item instead of consuming it.
 * [replacement] stands in for the crafting remainder of every item used up, or is left behind once a damaged item breaks.
 * [preserve] lists data components the result copies from this slot
 */
@KeepGeneratedSerializer
@Serializable(with = SerializableRecipeIngredient.Serializer::class)
data class SerializableRecipeIngredient(
    val item: SerializableItemStack,
    val amount: Int = 1,
    val damage: Int = 0,
    val keep: Boolean = false,
    val replacement: SerializableItemStack? = null,
    val preserve: @Serializable(StringsSerializer::class) List<String> = emptyList(),
) {
    internal fun toIngredient(context: RecipeContext, enforceAmount: Boolean = false): RecipeIngredient? {
        if (item.prefab != null && item.tag != null) context.warn("sets both a prefab and a tag on an ingredient, the tag is ignored")
        val tag = item.tag?.takeIf { item.prefab == null }?.let { key ->
            itemTag(key) ?: return context.warn("references the unknown or empty item tag $key")
        }
        // A space marks an item provider reference such as "nexo some_item", not a geary prefab
        val prefab = item.prefab?.takeUnless { ' ' in it }?.let(PrefabKey::ofOrNull)

        val bare = item.copy(amount = null)
        val tagDisplay = tag?.let { it.itemTypes().resolve(Registry.ITEM).firstOrNull()?.createItemStack() }
        val display = tagDisplay ?: bare.toItemStackOrNull() ?: return context.warn("has an ingredient that could not be built")

        val amount = amount.coerceAtLeast(1)
        if (damage > 0 && amount > 1) return context.warn("takes durability off an ingredient, which cannot also require an amount of it")
        if (!keep && damage > 0 && display.remainingDurability() == null) return context.warn("takes durability off ${display.type}, which has none")
        if (keep && (damage > 0 || replacement != null)) context.warn("keeps an ingredient, so the damage or replacement it sets is ignored")
        val replacement = replacement?.takeUnless { keep }?.let { it.toItemStackOrNull() ?: return context.warn("has a replacement that could not be built") }

        val extras = bare.copy(prefab = null, tag = null, type = null).takeUnless { it == SerializableItemStack() }
        val isPlainMaterial = item.prefab == null && tag == null && extras == null
        val matcher = matcher(context, bare, display, prefab, tag, extras, isPlainMaterial)

        val enforced = enforceAmount && amount > 1
        val plainType = display.type.asItemType()?.takeIf { isPlainMaterial }
        val choice = when {
            !enforced && tag != null && extras == null -> tag
            !enforced && plainType != null -> RecipeChoice.itemType(plainType)
            else -> RecipeChoice.predicateChoice({ !it.isEmpty && (!enforced || it.amount >= amount) && matcher(it) }, display)
        }

        return RecipeIngredient(
            display = display,
            amount = amount,
            consumption = consumption(replacement),
            preserved = preserve.toPreservedComponents(context),
            isPrefab = prefab != null,
            matcher = matcher,
            choice = choice,
        )
    }

    private fun matcher(
        context: RecipeContext,
        bare: SerializableItemStack,
        display: ItemStack,
        prefab: PrefabKey?,
        tag: RecipeChoice.ItemTypeChoice?,
        extras: SerializableItemStack?,
        isPlainMaterial: Boolean,
    ): (ItemStack) -> Boolean {
        val identity: (ItemStack) -> Boolean = when {
            prefab != null -> { stack ->
                val data = stack.persistentDataContainer
                !data.isEmpty && with(context.world) { prefab in data.decodePrefabs() }
            }

            tag != null -> tag::test
            isPlainMaterial -> { stack -> stack.type == display.type }
            // The bare item fixes the material, checked first as building the item to compare against is costly
            else -> { stack -> stack.type == display.type && bare.matches(stack) }
        }
        if (extras == null || (prefab == null && tag == null)) return identity
        return { stack -> identity(stack) && extras.matches(stack) }
    }

    private fun consumption(replacement: ItemStack?): Consumption = when {
        keep -> Consumption.Keep
        damage > 0 -> Consumption.Damage(damage, replacement)
        else -> Consumption.Consume(replacement)
    }

    private fun itemTag(key: NamespacedKey): RecipeChoice.ItemTypeChoice? {
        val tagKey = TagKey.create(RegistryKey.ITEM, key)
        if (!Registry.ITEM.hasTag(tagKey)) return null
        return Registry.ITEM.getTag(tagKey).takeUnless { it.isEmpty }?.let { RecipeChoice.itemType(it) }
    }

    object Serializer : KSerializer<SerializableRecipeIngredient> {
        // Contextual so kaml hands over scalars as well as maps, a class descriptor would reject the shorthand
        override val descriptor = ContextualSerializer(SerializableRecipeIngredient::class).descriptor

        override fun deserialize(decoder: Decoder): SerializableRecipeIngredient {
            val yaml = decoder as? YamlInput ?: return generatedSerializer().deserialize(decoder)
            val node = yaml.node
            if (node !is YamlMap || node.getKey("item") == null) {
                return fromShorthand(yaml.yaml.decodeFromYamlNode(SerializableItemStackSerializer(), node))
            }

            val ingredient = yaml.yaml.decodeFromYamlNode(generatedSerializer(), node)
            val itemAmount = ingredient.item.amount
            return if (node.getKey("amount") == null && itemAmount != null) ingredient.copy(amount = itemAmount) else ingredient
        }

        override fun serialize(encoder: Encoder, value: SerializableRecipeIngredient) =
            generatedSerializer().serialize(encoder, value)

        // The amount and damage keys of a short item map read as the ingredient's own options, as an exact item damage is rarely wanted
        private fun fromShorthand(item: SerializableItemStack) = SerializableRecipeIngredient(
            item = item.copy(amount = null, damage = null),
            amount = item.amount ?: 1,
            damage = item.damage ?: 0,
        )
    }
}
