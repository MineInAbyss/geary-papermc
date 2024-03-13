package com.mineinabyss.geary.papermc.bridge.readers

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("geary:read.location")
class ReadLocation

fun GearyModule.createLocationReader() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        val read by source.get<ReadLocation>()
    }
).exec {
    event.entity.set(bukkit.location)

}
