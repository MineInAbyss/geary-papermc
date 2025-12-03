package com.mineinabyss.geary.papermc.spawning.listeners

import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnLocation
import com.mineinabyss.geary.papermc.spawning.spread_spawn.SpreadSpawnRepository
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.plugin.Plugin

// listen to chunk load event and spawn entity if its in the list of entities
class ListSpawnListener(
    private val spawns: SpreadSpawnRepository,
    private val plugin: Plugin,
) : Listener {

    @EventHandler
    suspend fun ChunkLoadEvent.onChunkLoad() {
        val chunkEntities = chunk.entities.toList()
        val list: List<SpreadSpawnLocation> = spawns.getSpawnsInChunk(chunk)

        for (spread: SpreadSpawnLocation in list) {
            val idToCheck = spread.id
            val alreadyExists = chunkEntities.any { entity ->
                val checkspread = entity.toGearyOrNull()?.get<SpreadSpawnLocation>() ?: return@any false
                checkspread.id == idToCheck
            }
            if (alreadyExists) {
                continue
            }
            spread.spawn()
        }
    }
}
