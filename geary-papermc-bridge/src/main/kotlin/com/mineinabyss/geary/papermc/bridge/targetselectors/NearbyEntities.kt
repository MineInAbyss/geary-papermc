package com.mineinabyss.geary.papermc.bridge.targetselectors

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:nearby_entities")
class NearbyEntities(
    val radius: Double
)

class NearbyEntitiesSelector : GearyListener() {
    private val Pointers.bukkit by get<BukkitEntity>().on(target)
//    private val Pointers.targets by get<EmittedTargets>().orNull().on(event)
    private val Pointers.reader by get<NearbyEntities>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val rad = reader.radius
        val targets = event.entity.get<EmittedTargets>() ?: EmittedTargets(emptyList())
        val newTargets = EmittedTargets(targets.targets + bukkit.location
            .getNearbyEntities(rad, rad, rad)
            .mapNotNull { it.toGearyOrNull().takeIf { it != target.entity } })
        event.entity.set(newTargets)
    }
}
