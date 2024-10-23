package com.mineinabyss.geary.papermc.features.common.getters

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.FunctionExpression
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:get_nearby_entities")
class GetNearbyEntities(
    val radius: Expression<Double>,
) : FunctionExpression<GearyEntity, List<GearyEntity>> {
    override fun ActionGroupContext.map(input: GearyEntity): List<GearyEntity> {
        val radius = eval(radius)
        val bukkit = input.get<BukkitEntity>() ?: return emptyList()
        val world = bukkit.world.toGeary()
        with(world) {
            return bukkit
                .getNearbyEntities(radius, radius, radius)
                .mapNotNull { it.toGearyOrNull().takeIf { it != entity } }
        }
    }
}
