package com.mineinabyss.geary.papermc.features.items.variants

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.tracking.items.components.CustomModelDataVariants
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

/**
 * Switches the held item to its next [CustomModelDataVariants] string.
 * After the last one it goes back to no custom_model_data, the same order Nexo furniture-states cycle in
 */
@Serializable
@SerialName("geary:cycle_custom_model_data")
class CycleCustomModelDataAction : Action {
    override fun ActionGroupContext.execute() {
        val itemEntity = entity ?: return
        val variants = itemEntity.get<CustomModelDataVariants>()?.takeIf { it.strings.isNotEmpty() } ?: return
        val player = itemEntity.parent?.get<Player>() ?: return
        val inventory = player.inventory.toGeary() ?: return
        val hand = listOf(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).firstOrNull { inventory.get(it) == itemEntity } ?: return
        val item = player.inventory.getItem(hand)

        val next = variants.strings.getOrNull(variants.strings.indexOf(variants.current(item)) + 1)
        if (next == null) item.unsetData(DataComponentTypes.CUSTOM_MODEL_DATA)
        else item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString(next).build())
    }
}
