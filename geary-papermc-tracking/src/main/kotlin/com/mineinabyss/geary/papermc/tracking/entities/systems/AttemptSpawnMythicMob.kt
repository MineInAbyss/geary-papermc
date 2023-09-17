package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit


class AttemptSpawnMythicMob : GearyListener() {
    private val Pointers.mobType by get<SetMythicMob>().on(target)
    private val Pointers.attemptSpawn by get<AttemptSpawn>().on(event)

    val Pointers.family by family {
        not { has<BukkitEntity>() }
    }.on(target)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobType.id).orElse(null) ?: return
        mythicMob.spawn(BukkitAdapter.adapt(attemptSpawn.location), 1.0, SpawnReason.NATURAL) { mob ->
            target.entity.set(mob)
        }
    }
}
