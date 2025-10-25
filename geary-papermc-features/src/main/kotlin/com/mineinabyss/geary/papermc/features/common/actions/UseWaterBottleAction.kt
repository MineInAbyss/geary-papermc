package com.mineinabyss.geary.papermc.features.common.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Material
import org.bukkit.Sound.ENTITY_GENERIC_SPLASH
import org.bukkit.Sound.ITEM_BOTTLE_EMPTY
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:water_bottle")
class UseWaterBottleAction(
    val cauldronLevelIncrease: Int = 1
): Action {
    override fun ActionGroupContext.execute() {
        val player = entity?.get<Player>() ?: return
        val targetedBlock = player.rayTraceBlocks(3.0)?.hitBlock ?: return
        val container = WorldGuard.getInstance().platform.regionContainer
        val query = container.createQuery()
        val location = BukkitAdapter.adapt(targetedBlock.location)
        var success = false
        
        if (cauldronLevelIncrease > 3) return
        val authorized = query.testState(location, WorldGuardPlugin.inst().wrapPlayer(player), com.sk89q.worldguard.protection.flags.Flags.BUILD)
        if (!authorized) return
        if (targetedBlock.type == Material.CAULDRON) {
            targetedBlock.blockData = Material.WATER_CAULDRON.createBlockData {
                (it as Levelled).level = cauldronLevelIncrease
            }
            success = true
        } else if (targetedBlock.type == Material.WATER_CAULDRON) {
            val cauldronData = targetedBlock.blockData as Levelled
            val newLevel = (cauldronData.level + cauldronLevelIncrease).coerceAtMost(3)
            cauldronData.level = newLevel
            targetedBlock.blockData = cauldronData
        }

        if (targetedBlock.type == Material.DIRT) {
            targetedBlock.type = Material.MUD
            success = true
        }
        if (success) {
            player.playSound(player, ITEM_BOTTLE_EMPTY, 0.5f, 0.5f)
            player.playSound(player, ENTITY_GENERIC_SPLASH, 0.5f, 0.5f)
        }
    }
}
