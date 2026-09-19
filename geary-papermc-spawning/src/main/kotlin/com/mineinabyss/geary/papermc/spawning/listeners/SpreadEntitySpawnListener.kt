package com.mineinabyss.geary.papermc.spawning.listeners

import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent

// EntitiesLoadEvent rather than ChunkLoadEvent, since persisted entities are not in yet at chunk load
class ListSpawnListener(
    private val spawns: SpreadSpawnRepository,
    private val mainWorld: World,
) : Listener {

    @EventHandler
    suspend fun EntitiesLoadEvent.onEntitiesLoad() {
        if (chunk.world != mainWorld) return
        val existingIds = entities.mapNotNullTo(HashSet()) { it.toGearyOrNull()?.get<SpreadSpawnLocation>()?.id }
        val stored = spawns.getSpawnsInChunk(chunk)

        // The entity section may have unloaded while the query ran
        if (!chunk.isEntitiesLoaded) return
        for (spread in stored) {
            if (spread.id in existingIds) continue
            spread.spawn()
        }
    }
}
