package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit


class AttemptSpawnMythicMob : GearyListener() {
    private val TargetScope.mobType by get<SetMythicMob>()
    private val EventScope.attemptSpawn by get<AttemptSpawn>()

    val TargetScope.family by family {
        not { has<BukkitEntity>() }
    }

    @Handler
    fun TargetScope.handle(event: EventScope) {
        val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobType.id).orElse(null) ?: return
        mythicMob.spawn(BukkitAdapter.adapt(event.attemptSpawn.location), 1.0, SpawnReason.NATURAL) { mob ->
            entity.set(mob)
        }
    }
}
