package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.tracking.items.components.SetItemIgnoredProperties
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.nms.nbt.fastPDC
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.SmithingTransformRecipe
import java.util.*

class RecipeCraftingListener : Listener {
    /**
     * Prevents custom items being usable in vanilla recipes based on their material,
     * when they have a [DenyInVanillaRecipes] component, by setting result to null.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun PrepareItemCraftEvent.onCraftWithCustomItem() {
        // Ensure this only cancels vanilla recipes
        if (recipe == null || (recipe as? Keyed)?.key()?.namespace() != "minecraft") return

        if (inventory.matrix.any {
                it?.itemMeta?.persistentDataContainer
                    ?.decodePrefabs()
                    ?.firstOrNull()
                    ?.toEntityOrNull()
                    ?.has<DenyInVanillaRecipes>() == true
            }) {
            inventory.result = null
        }
    }

    @EventHandler
    fun PrepareSmithingEvent.onCustomSmithingTransform() {
        // Smithing will cache the last recipe, so even with 0 input
        // recipe will return as not null if say a Diamond Hoe was put in before
        if (inventory.contents.any { it?.isEmpty != false }) return
        // Return if no item is custom, as then vanilla should handle it fine
        if (inventory.contents.none { it?.fastPDC?.hasComponentsEncoded == true }) return

        val (template, mineral) = (inventory.inputTemplate ?: return) to (inventory.inputMineral ?: return)
        val equipment = inventory.inputEquipment ?: return

        val inputGearyEntity = equipment.fastPDC?.decodePrefabs()?.firstOrNull() ?: return
        val smithingTransformRecipes = Bukkit.recipeIterator().asSequence()
            .filter { (it as? SmithingTransformRecipe)?.result?.fastPDC?.hasComponentsEncoded == true }
            .filterIsInstance<SmithingTransformRecipe>()
        val customRecipeResult = smithingTransformRecipes.filter {
            it.template.test(template) && it.addition.test(mineral) && it.base.itemStack.itemMeta?.persistentDataContainer?.decodePrefabs()
                ?.firstOrNull() == inputGearyEntity
        }.firstOrNull()?.result
        var recipeResultItem = (customRecipeResult ?: ItemStack.empty()).let {
            result?.toSerializable()?.toItemStack(it, EnumSet.of(BaseSerializableItemStack.Properties.DISPLAY_NAME))
        }

        recipeResultItem = recipeResultItem?.editItemMeta {
            displayName(
                equipment.fastPDC?.decode<SetItemIgnoredProperties>()?.let { properties ->
                    persistentDataContainer.encode(properties)
                    if (BaseSerializableItemStack.Properties.DISPLAY_NAME in properties.ignore && result?.itemMeta?.hasDisplayName() == true)
                        result?.itemMeta?.displayName()?.compact()
                    else displayName()?.compact()
                } ?: displayName()?.compact()
            )
        }

        result = recipeResultItem
    }
}
