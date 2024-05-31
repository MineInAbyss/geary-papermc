package com.mineinabyss.geary.papermc.mythicmobs.spawning

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import io.lumine.mythic.api.mobs.entities.SpawnReason
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import kotlin.jvm.optionals.getOrNull

fun GearyModule.mythicMobSpawner() = observeWithData<AttemptSpawn>()
    .exec(query<SetMythicMob>()) { (mobType) ->
        val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobType.id).getOrNull() ?: return@exec
        mythicMob.spawn(BukkitAdapter.adapt(event.location), 1.0, SpawnReason.NATURAL) { mob ->
            entity.set(mob)
            entity.set(mythicMob)
        }
    }
