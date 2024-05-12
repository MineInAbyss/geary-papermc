package com.mineinabyss.geary.papermc.mythicmobs.spawning

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.mythicmobs.mobzyMythicMobs
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import io.lumine.mythic.bukkit.BukkitAPIHelper
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@JvmInline
@Serializable
@SerialName("geary:bind.mythic_mob")
value class BindToMythicMob(val id: String)

class BindToMythicMobSystem : Listener {
    @EventHandler
    fun MythicMobSpawnEvent.bindMythicOnSpawn() {
        val name = mobType.internalName
        val binds = mobzyMythicMobs.mythicMobBinds[name]
        val gearyEntity = entity.toGearyOrNull() ?: return
        binds.forEach { bind -> gearyEntity.extend(bind) }
        if (binds.isNotEmpty())
            geary.logger.d("Mythic mob bind: Bound $name to $binds (MythicMobSpawnEvent)")
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun GearyEntityAddToWorldEvent.bindMythicOnSubsequentLoad() {
        val instance = BukkitAPIHelper().getMythicMobInstance(entity) ?: return
        val name = instance.type.internalName
        val binds = mobzyMythicMobs.mythicMobBinds[name]
        binds.forEach { bind -> gearyEntity.extend(bind) }
        if (binds.isNotEmpty())
            geary.logger.d("Mythic mob bind: Bound $name to $binds (Entity add event)")
    }
}
