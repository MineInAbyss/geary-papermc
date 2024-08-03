package com.mineinabyss.geary.papermc.features.common.getters

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.FunctionExpression
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location


@Serializable
@SerialName("geary:get_location")
class GetLocation : FunctionExpression<GearyEntity, Location?> {
    override fun ActionGroupContext.map(input: GearyEntity): Location? {
        return input.get<BukkitEntity>()?.location
    }
}
