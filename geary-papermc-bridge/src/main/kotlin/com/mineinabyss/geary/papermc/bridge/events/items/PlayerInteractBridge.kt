package com.mineinabyss.geary.papermc.bridge.events.items

import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class OnInteract(
    val leftClicked: Boolean? = null,
    val rightClicked: Boolean? = null,
)

class PlayerInteractBridge : Listener {
    private val rightClickCooldowns = Int2IntOpenHashMap()

    @EventHandler
    fun PlayerInteractEvent.onClick() {
        val heldItem = player.inventory.toGeary()?.itemInMainHand ?: return

        EventHelpers.runSkill<OnInteract>(heldItem,
            conditions = {
                if(it.leftClicked != null) {
                    if(leftClicked != it.leftClicked) return@runSkill false
                }
                if(it.rightClicked != null) {
                    // Right click gets fired twice, so we manually prevent two right-clicks within several ticks of each other.
                    val currTick = Bukkit.getServer().currentTick
                    val eId = player.entityId
                    val cooldownRightClicked = rightClicked && currTick - rightClickCooldowns[eId] > 3
                    if (cooldownRightClicked) {
                        rightClickCooldowns[eId] = currTick
                    }

                    if(cooldownRightClicked != it.rightClicked) return@runSkill false
                }
                true
            }
        )
    }
}
