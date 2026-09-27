package com.mineinabyss.geary.papermc.features.items.transform

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.features.common.inventory.equippedItems
import com.mineinabyss.geary.papermc.item
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class TransformOnPickupListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun EntityPickupItemEvent.transformPickedUpItem() {
        val player = entity as? Player ?: return
        val transforms = player.inventory.toGeary()?.equippedItems()
            ?.flatMap { it.get<TransformOnPickup>()?.transforms ?: emptyList() }
            ?.takeIf { it.isNotEmpty() } ?: return

        val pickedUp = item.itemStack
        with(player.world.toGeary()) {
            itemEntityContext {
                val context = ActionGroupContext().apply {
                    this.entity = pickedUp.toGearyOrNull()
                    this.item = pickedUp
                    this.location = player.location
                    register("player", player.toGeary())
                }
                val transform = transforms
                    .firstOrNull { transform -> transform.conditions.all { it.conditionsMet(context) } } ?: return

                // The stack on the event only counts what fits, replacing it skips the server putting the rest back
                val output = transform.output.toItemStack().apply { amount = pickedUp.amount + remaining }
                item.itemStack = output

                context.register("output", output)
                transform.actions?.execute(context)
            }
        }
    }
}
