package com.mineinabyss.geary.papermc.tracking.entities.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.nms.aliases.toBukkit
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.geary.papermc.tracking.entities.components.AttemptSpawn
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.MobSpawnType
import org.bukkit.event.entity.CreatureSpawnEvent

class AttemptSpawnListener : GearyListener() {
    private val TargetScope.mobType by onSet<SetEntityType>()
    private val EventScope.attemptSpawn by get<AttemptSpawn>()

    val TargetScope.family by family {
        not { has<BukkitEntity>() }
    }

    @Handler
    fun TargetScope.handle(event: EventScope) {
        val loc = event.attemptSpawn.location
        val world = loc.world.toNMS()
        val mob = mobType.entityTypeFromRegistry.spawn(
            world,
            BlockPos(loc.x, loc.y, loc.z),
            MobSpawnType.COMMAND,
            CreatureSpawnEvent.SpawnReason.COMMAND
        )?.toBukkit() ?: return

        entity.set(mob)
    }
}
