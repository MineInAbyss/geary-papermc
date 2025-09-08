package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:bonemeal_radius")
class BoneMealRadiusAction(
    val radius: Int,
    val allowCrops: Boolean,
    val allowSaplings: Boolean,
    val excludedBlocks: Set<String>,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = entity?.get<Player>() ?: return
        val center = player.location.block
        val world = player.world

        val allowedBlockTags = mutableSetOf<Tag<Material>>()
        if (allowCrops) allowedBlockTags.add(Tag.CROPS)
        if (allowSaplings) allowedBlockTags.add(Tag.SAPLINGS)

        val excludedMaterials = excludedBlocks.mapNotNull(Material::matchMaterial)
        for (x in -radius..radius) for (y in -radius..radius) for (z in -radius..radius) {
            val block = world.getBlockAt(center.x + x, center.y + y, center.z + z)
            if (block.type !in excludedMaterials && allowedBlockTags.any { it.isTagged(block.type) }) {
                block.applyBoneMeal(BlockFace.UP)
            }
        }
    }
}
