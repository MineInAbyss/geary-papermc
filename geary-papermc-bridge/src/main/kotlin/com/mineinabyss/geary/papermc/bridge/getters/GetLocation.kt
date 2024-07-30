package com.mineinabyss.geary.papermc.bridge.getters

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.LivingEntity


@Serializable
@SerialName("geary:get.location")
class GetLocation : Action {
    override fun ActionGroupContext.execute(): Location? = entity.get<BukkitEntity>()?.location
}
