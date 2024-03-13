package com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import kotlin.jvm.optionals.getOrNull

fun GearyModule.createAttemptSpawnMythicMobListener() = listener(
    object : ListenerQuery() {
        val mobType by get<SetMythicMob>()
        val attemptSpawn by event.get<AttemptSpawn>()
        override fun ensure() = this { not { has<BukkitEntity>() } }
    }
).exec {
    val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobType.id).getOrNull() ?: return@exec
    mythicMob.spawn(BukkitAdapter.adapt(attemptSpawn.location), 1.0, SpawnReason.NATURAL) { mob ->
        entity.set(mob)
        entity.set(mythicMob)
    }
}
