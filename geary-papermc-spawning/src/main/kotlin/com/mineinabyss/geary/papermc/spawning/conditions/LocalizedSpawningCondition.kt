package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location


@Serializable
@SerialName("geary:spawn_region")
class LocalizedSpawningCondition(
    val region_name: String,
    val first_corner: @Serializable(LocationAltSerializer::class) Location,
    val second_corner: @Serializable(LocationAltSerializer::class) Location,
): Condition {

    private val min_xyz = Triple(
        minOf(first_corner.x, second_corner.x),
        minOf(first_corner.y, second_corner.y),
        minOf(first_corner.z, second_corner.z)
    )
    private val max_xyz = Triple(
        maxOf(first_corner.x, second_corner.x),
        maxOf(first_corner.y, second_corner.y),
        maxOf(first_corner.z, second_corner.z)
    )
    override fun ActionGroupContext.execute(): Boolean {
        val location = location ?: return false
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        if (x < min_xyz.first || x > max_xyz.first) return false
        if (y < min_xyz.second || y > max_xyz.second) return false
        if (z < min_xyz.third || z > max_xyz.third) return false
        return true
    }


}