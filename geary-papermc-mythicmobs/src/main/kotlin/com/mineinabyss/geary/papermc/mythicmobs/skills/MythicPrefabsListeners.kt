package com.mineinabyss.geary.papermc.mythicmobs.skills

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.papermc.mythicmobs.GearyMythicConfigOptions.addPrefabs
import com.mineinabyss.geary.papermc.mythicmobs.GearyMythicConfigOptions.prefabs
import com.mineinabyss.geary.papermc.mythicmobs.MythicEmbeddedGearyEntity
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import io.lumine.mythic.bukkit.events.MythicTriggerEvent
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.skills.SkillTriggers
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicPrefabsListeners(
    val logger: Logger,
) : Listener {
    @EventHandler
    fun MythicMechanicLoadEvent.onMechanicLoad() {
        when (mechanicName.lowercase()) {
            "prefabs" -> register(PrefabsMechanic(config))
        }
    }

    private fun addPrefabs(mob: ActiveMob, event: String) {
        val bukkit = BukkitAdapter.adapt(mob.entity)
        val inherit = mob.type.prefabs
        val embedded = MythicEmbeddedGearyEntity.getOrLoadEmbeddedPrefab(bukkit.world.toGeary(), mob.type)
        if (embedded != null) {
            bukkit.toGeary().extend(embedded)
            logger.d { "$event event - ${mob.type.internalName} loaded an embedded prefab." }
        }

        mob.addPrefabs(inherit)
        if (inherit.isNotEmpty())
            logger.d { "$event event - ${mob.type.internalName} added prefabs: $inherit" }
    }

    @EventHandler
    fun MythicMobSpawnEvent.onSpawn() {
        addPrefabs(mob, "Spawn")
    }

    @EventHandler
    fun MythicTriggerEvent.onLoad() {
        if (trigger != SkillTriggers.LOAD) return
        val mob = skillMetadata.caster as? ActiveMob ?: return
        addPrefabs(mob, "Load")
    }
}
