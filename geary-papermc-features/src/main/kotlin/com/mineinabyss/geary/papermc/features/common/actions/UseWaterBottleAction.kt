package com.mineinabyss.geary.papermc.features.common.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import org.bukkit.Material
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

        val authorized = query.testState(location, WorldGuardPlugin.inst().wrapPlayer(player), com.sk89q.worldguard.protection.flags.Flags.BUILD)
        if (!authorized) return
        if (targetedBlock.type == Material.CAULDRON) {
            targetedBlock.type = Material.WATER_CAULDRON
            val cauldronData = targetedBlock.blockData
            if (cauldronData is Levelled) {
                cauldronData.level = cauldronLevelIncrease
                targetedBlock.blockData = cauldronData
            }
        } else if (targetedBlock.type == Material.WATER_CAULDRON) {
            val cauldronData = targetedBlock.blockData
            if (cauldronData is Levelled) {
                val newLevel = if (cauldronData.level + cauldronLevelIncrease > 3) 3 else cauldronData.level + cauldronLevelIncrease
                cauldronData.level = newLevel
                targetedBlock.blockData = cauldronData
            }
        }

        if (targetedBlock.type == Material.DIRT) {
            targetedBlock.type = Material.MUD
        }
    }
}