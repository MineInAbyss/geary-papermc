package com.mineinabyss.geary.papermc.tracking.entities.systems.missingprefabs

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.findEntities
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.UpdateMob
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MissingPrefabsListener(private val config: GearyPaperConfig) : Listener {
    @EventHandler
    fun GearyEntityAddToWorldEvent.onAdd() {
        // Entities added while prefabs are still loading are picked up by resolveStoredPrefabs once loading ends
        if (!PrefabLoading.awaitLoaded().isDone) return
        with(entity.world.toGeary()) { MissingPrefabs.handle(entity, gearyEntity, config) }
    }
}

object MissingPrefabs {
    private val warnedKeys = mutableSetOf<PrefabKey>()

    // Entities tracked before prefabs finished loading had their saved prefabs silently skipped
    context(world: WorldScoped)
    fun resolveStoredPrefabs(config: GearyPaperConfig) = with(world) {
        findEntities { has<BukkitEntity>() }.forEach { gearyEntity ->
            val bukkit = gearyEntity.get<BukkitEntity>() ?: return@forEach
            if (bukkit is Player || !bukkit.isValid) return@forEach
            val stored = bukkit.persistentDataContainer.decodePrefabs()
            if (stored.isEmpty()) return@forEach

            val linked = gearyEntity.prefabs.mapNotNull { it.get<PrefabKey>() }.toSet()
            if (stored.any { it !in linked && entityOfOrNull(it) != null }) UpdateMob.recreateGearyEntity(bukkit)
            else handle(bukkit, gearyEntity, config)
        }
    }

    context(world: WorldScoped)
    fun handle(bukkit: BukkitEntity, gearyEntity: GearyEntity, config: GearyPaperConfig) = with(world) {
        if (bukkit is Player) return@with
        // Prefabs embedded in MythicMobs configs only register once that mob type first spawns or loads
        val missing = bukkit.persistentDataContainer.decodePrefabs().filter { it.namespace != "mythicmobs" && entityOfOrNull(it) == null }
        if (missing.isEmpty()) return@with

        // MythicMobs keeps its own data under its plugin namespace and restores the mob after Geary sees it
        val ownedByMythic = bukkit.persistentDataContainer.keys.any { it.namespace == "mythicmobs" }
        // Furniture and other non-mob entities still render without their prefab, so only mobs are removed
        val remove = config.entities.removeMobsWithMissingPrefabs && bukkit is Mob && gearyEntity.prefabs.isEmpty() && !ownedByMythic
        val loc = bukkit.location
        val where = "${bukkit.type} ${bukkit.uniqueId} at ${loc.world.name} ${loc.blockX} ${loc.blockY} ${loc.blockZ}"

        if (remove) {
            logger.i("Removing $where, its saved prefabs $missing no longer exist")
            UpdateMob.scheduleRemove(bukkit)
            return@with
        }

        val firstSeen = missing.filter { warnedKeys.add(it) }
        if (firstSeen.isNotEmpty()) logger.w(
            """
                $where has saved prefabs $firstSeen that no longer exist, so it loads as a plain ${bukkit.type}
                Set entities.removeMobsWithMissingPrefabs to true to remove such mobs as they load
            """.trimIndent()
        ) else logger.d("$where has missing saved prefabs $missing")
    }
}
