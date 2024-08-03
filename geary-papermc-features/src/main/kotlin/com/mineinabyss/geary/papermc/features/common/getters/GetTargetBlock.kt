package com.mineinabyss.geary.papermc.features.common.getters

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.FunctionExpression
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.LivingEntity


@Serializable
@SerialName("geary:get_target_block")
class GetTargetBlock(
    val maxDistance: Expression<Int>,
) : FunctionExpression<GearyEntity, Location?> {
    override fun ActionGroupContext.map(input: GearyEntity): Location? {
        val bukkit = entity.get<BukkitEntity>() ?: return null
        return (bukkit as? LivingEntity)?.getTargetBlock(null, eval(maxDistance))?.location
    }
}
