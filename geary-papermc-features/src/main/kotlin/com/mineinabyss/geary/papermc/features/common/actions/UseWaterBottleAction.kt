package com.mineinabyss.geary.papermc.features.common.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.regions.GearyRegions
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
        var success = false

        if (cauldronLevelIncrease > 3) return
        val regions = GearyRegions.forConfig(gearyPaper.config.regionProvider)
        if (regions != null && !regions.canBuild(player, targetedBlock.location)) return
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
