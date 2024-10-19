package com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observeWithData
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.nms.aliases.toBukkit
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.typealiases.BukkitEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.MobSpawnType
import org.bukkit.event.entity.CreatureSpawnEvent

fun Geary.createAttemptSpawnListener() = observeWithData<AttemptSpawn>()
    .exec(query<SetEntityType> { not { has<BukkitEntity>() } }) { (mobType) ->
        val loc = event.location
        mobType.entityTypeFromRegistry.spawn(
            loc.world.toNMS(),
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
