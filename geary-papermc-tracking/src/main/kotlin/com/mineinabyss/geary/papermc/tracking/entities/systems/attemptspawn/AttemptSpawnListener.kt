package com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.nms.aliases.toBukkit
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.typealiases.BukkitEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.MobSpawnType
import org.bukkit.event.entity.CreatureSpawnEvent

fun GearyModule.createAttemptSpawnListener() = listener(
    object : ListenerQuery() {
        val mobType by get<SetEntityType>()
        val attemptSpawn by event.get<AttemptSpawn>()
        override fun ensure() = this { not { has<BukkitEntity>() } }
    }
).exec {
    val loc = attemptSpawn.location
    mobType.entityTypeFromRegistry.spawn(
        loc.world.toNMS(),
        null,
        // We set the entity here so that we don't create a separate Geary entity in EntityWorldEventTracker
        // This is called before adding to the world.
        { mob -> entity.set(mob.toBukkit()) },
        BlockPos(loc.blockX, loc.blockY, loc.blockZ),
        MobSpawnType.NATURAL,
        false,
        false,
        CreatureSpawnEvent.SpawnReason.COMMAND
    )
}
