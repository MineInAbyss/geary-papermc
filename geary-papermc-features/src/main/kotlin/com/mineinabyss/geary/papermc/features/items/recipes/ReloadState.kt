package com.mineinabyss.geary.papermc.features.items.recipes

import net.minecraft.world.item.crafting.RecipeHolder
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

/** Recipes taken off the server through removeRecipes, kept to put back once no prefab removes them anymore */
internal class RemovedRecipes {
    private val removed = linkedMapOf<NamespacedKey, RecipeHolder<*>>()

    operator fun contains(key: NamespacedKey) = key in removed

    fun add(key: NamespacedKey, holder: RecipeHolder<*>) {
        removed[key] = holder
    }

    fun release(requested: Set<NamespacedKey> = emptySet()): List<RecipeHolder<*>> {
        val released = removed.filterKeys { it !in requested }
        removed -= released.keys
        return released.values.toList()
    }
}

/** A reload whose state equals the one players were last sent does not need to resend recipes */
internal data class SentState(val layout: RecipeRegistry.Layout, val fingerprints: Map<NamespacedKey, RecipeFingerprint>)

internal data class RecipeFingerprint(
    val definition: Any,
    val result: ItemStack,
    val group: String,
    val category: String,
    // Resolved items, as a prefab's item can change without the definition referencing it changing
    val ingredients: List<ItemStack>,
)
