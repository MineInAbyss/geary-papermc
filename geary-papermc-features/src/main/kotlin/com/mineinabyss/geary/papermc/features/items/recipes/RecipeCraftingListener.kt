package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.nms.nbt.fastPDC
import com.mineinabyss.idofront.serialization.toSerializable
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.SmithingTransformRecipe

class RecipeCraftingListener : Listener {
    /**
     * Prevents custom items being usable in vanilla recipes based on their material,
     * when they have a [DenyInVanillaRecipes] component, by setting result to null.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PrepareItemCraftEvent.onCraftWithCustomItem() = with((inventory.holder as Player).world.toGeary()) {
        // Ensure this only cancels vanilla recipes
        if (recipe == null || (recipe as? Keyed)?.key()?.namespace() != "minecraft") return

        if (inventory.matrix.any {
                entityOfOrNull(
                    it?.itemMeta?.persistentDataContainer
                        ?.decodePrefabs()
                        ?.firstOrNull()
                )
                    ?.has<DenyInVanillaRecipes>() == true
            }) {
            inventory.result = null
        }
    }

    @EventHandler
    fun PrepareSmithingEvent.onCustomSmithingTransform() = with((inventory.viewers.first() as Player).world.toGeary()) {
        // Smithing will cache the last recipe, so even with 0 input
        // recipe will return as not null if say a Diamond Hoe was put in before
        if (inventory.contents.any { it?.isEmpty != false }) return
        // Return if no item is custom, as then vanilla should handle it fine
        if (inventory.contents.none { it?.fastPDC?.hasComponentsEncoded == true }) return

        val (template, mineral) = (inventory.inputTemplate ?: return) to (inventory.inputMineral ?: return)
        val equipment = inventory.inputEquipment ?: return

        val inputGearyEntity = equipment.fastPDC?.decodePrefabs()?.firstOrNull() ?: return
        val smithingTransformRecipes = Bukkit.recipeIterator().asSequence()
            .filterIsInstance<SmithingTransformRecipe>()
            .filter { it.result.fastPDC?.hasComponentsEncoded == true }
        val customRecipeResult = smithingTransformRecipes.filter {
            it.base.itemStack.itemMeta?.persistentDataContainer?.decodePrefabs()?.firstOrNull() == inputGearyEntity
                    && it.template.test(template) && it.addition.test(mineral)
        }.firstOrNull()?.result

        result = (customRecipeResult ?: ItemStack.empty()).let {
            result?.toSerializable()?.toItemStack(it)
        }
    }
}
