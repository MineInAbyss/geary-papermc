package com.mineinabyss.geary.papermc.bridge.readers

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity


@Serializable
@SerialName("geary:read.location")
class ReadLocation

class ReadLocationSystem : GearyListener() {
    private val Pointers.bukkit by get<BukkitEntity>().on(target)
    private val Pointers.read by get<ReadLocation>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        event.entity.set(bukkit.location)
    }
}
