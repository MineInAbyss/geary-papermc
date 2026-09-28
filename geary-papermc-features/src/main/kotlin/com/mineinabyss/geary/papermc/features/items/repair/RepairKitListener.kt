package com.mineinabyss.geary.papermc.features.items.repair

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.item
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCreativeEvent

class RepairKitListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun InventoryClickEvent.repairWithKit() {
        // The creative client manages its own cursor, so server side changes to it get overwritten
        if (this is InventoryCreativeEvent || click != ClickType.RIGHT) return
        val player = whoClicked as? Player ?: return
        val kit = cursor.takeIf { !it.isEmpty } ?: return
        val target = currentItem?.takeIf { !it.isEmpty } ?: return

        val damage = target.getData(DataComponentTypes.DAMAGE) ?: return
        if (damage <= 0 || target.hasData(DataComponentTypes.UNBREAKABLE)) return

        with(player.world.toGeary()) {
            itemEntityContext {
                val repairKit = kit.toGearyOrNull()?.get<RepairKit>() ?: return
                val context = ActionGroupContext().apply {
                    entity = target.toGearyOrNull()
                    item = target
                    location = player.location
                    register("player", player.toGeary())
                }
                if (!repairKit.conditions.all { it.conditionsMet(context) }) return
                isCancelled = true

                val kitMaxDamage = kit.getData(DataComponentTypes.MAX_DAMAGE)
                val kitDamage = kit.getData(DataComponentTypes.DAMAGE) ?: 0
                val kitRemaining = if (kitMaxDamage == null) damage else kitMaxDamage - kitDamage
                val repaired = damage.coerceAtMost(kitRemaining)
                if (repaired <= 0) return

                target.setData(DataComponentTypes.DAMAGE, damage - repaired)
                if (kitMaxDamage == null || repaired == kitRemaining) kit.subtract()
                else kit.setData(DataComponentTypes.DAMAGE, kitDamage + repaired)

                currentItem = target
                view.setCursor(kit)

                context.register("repaired", repaired)
                repairKit.actions?.execute(context)
            }
        }
    }
}
