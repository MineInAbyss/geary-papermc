package com.mineinabyss.geary.papermc.bridge.targetselectors

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:nearby_entities")
class NearbyEntities(
    val radius: Double
)

fun GearyModule.createNearbyEntitiesSelector() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        val reader by source.get<NearbyEntities>()
    }
).exec {
    val rad = reader.radius
    val targets = event.entity.get<EmittedTargets>() ?: EmittedTargets(emptyList())
    val newTargets = EmittedTargets(targets.targets + bukkit.location
        .getNearbyEntities(rad, rad, rad)
        .mapNotNull { it.toGearyOrNull().takeIf { it != entity } })
    event.entity.set(newTargets)
}
