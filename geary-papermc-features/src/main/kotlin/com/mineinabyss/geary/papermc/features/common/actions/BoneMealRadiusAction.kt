package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.tags.BlockTags
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player


@Serializable
@SerialName("geary:bonemeal_radius")
class BoneMealRadiusAction(
    val radius: Int,
    val AllowedTags: Set<String>,
    val excludedBlocks: Set<String>,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = entity?.get<Player>() ?: return println("No player entity found for BoneMealRadiusAction")
        val center = player.location.block
        val world = player.world

        val allowedBlockTags = mutableSetOf<Tag<Material>>()
        for (tag in AllowedTags) {
            when (tag.lowercase()) {
                "saplings" -> allowedBlockTags.add(Tag.SAPLINGS)
                "crops" -> allowedBlockTags.add(Tag.CROPS)
            }
        }
        val excludedMaterials = excludedBlocks.mapNotNull { runCatching { Material.valueOf(it.uppercase()) }.getOrNull() }.toSet()

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(center.x + x, center.y + y, center.z + z)
                    if (allowedBlockTags.any { it.isTagged(block.type) } && block.type !in excludedMaterials) {
                        block.applyBoneMeal(BlockFace.UP)
                    }
                }
            }
        }
    }
}

