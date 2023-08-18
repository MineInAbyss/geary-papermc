package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.nms.aliases.toBukkit
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.typealiases.BukkitEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.MobSpawnType
import org.bukkit.event.entity.CreatureSpawnEvent

class AttemptSpawnListener : GearyListener() {
    private val TargetScope.mobType by get<SetEntityType>()
    private val EventScope.attemptSpawn by get<AttemptSpawn>()

    val TargetScope.family by family {
        not { has<BukkitEntity>() }
    }


    @Handler
    fun TargetScope.handle(event: EventScope) {
        val loc = event.attemptSpawn.location
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
}
