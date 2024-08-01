package com.mineinabyss.geary.papermc.features.common.getters

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:get.nearby_entities")
class GetNearbyEntities(
    val radius: Expression<Double>,
) : Action {
    override fun ActionGroupContext.execute(): List<GearyEntity> {
        val radius = eval(radius)
        val bukkit = entity.get<BukkitEntity>() ?: return emptyList()
        return bukkit
            .getNearbyEntities(radius, radius, radius)
            .mapNotNull { it.toGearyOrNull().takeIf { it != entity } }
    }
}
