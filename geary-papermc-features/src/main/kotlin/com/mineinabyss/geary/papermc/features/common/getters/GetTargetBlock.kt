package com.mineinabyss.geary.papermc.features.common.getters

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.LivingEntity


@Serializable
@SerialName("geary:get.target_block")
class GetTargetBlock(
    val maxDistance: Expression<Int>,
) : Action {
    override fun ActionGroupContext.execute(): Location? {
        val bukkit = entity.get<BukkitEntity>() ?: return null
        return (bukkit as? LivingEntity)?.getTargetBlock(null, eval(maxDistance))?.location
    }
}
