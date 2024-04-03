package com.mineinabyss.geary.papermc.tracking.entities.systems.boundingbox

import net.minecraft.world.entity.EntityType
import org.bukkit.util.BoundingBox

object BoundingBoxHelpers {

    fun getForEntityType(entityType: EntityType<*>): BoundingBox {
        val aabb = entityType.dimensions.makeBoundingBox(0.0, 0.0, 0.0)

        return BoundingBox(
            aabb.minX,
            aabb.minY,
            aabb.minZ,
            aabb.maxX,
            aabb.maxY,
            aabb.maxZ,
        )
    }
}
