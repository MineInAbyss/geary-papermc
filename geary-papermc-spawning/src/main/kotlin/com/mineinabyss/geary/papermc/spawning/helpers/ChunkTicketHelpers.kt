package com.mineinabyss.geary.papermc.spawning.helpers

import com.mineinabyss.geary.papermc.gearyPaper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.bukkit.Chunk

/**
 * Adds a plugin ticket to this chunk, ensuring it doesn't unload until [block] completes.
 */
inline fun <T> Chunk.withTicket(block: (Chunk) -> T): T = try {
    addPluginChunkTicket(gearyPaper.plugin)
    block(this)
} finally {
    removePluginChunkTicket(gearyPaper.plugin)
}

/**
 * Launches a coroutine on the main server thread, adding a plugin ticket to this chunk,
 * ensuring it doesn't unload until the coroutine completes or is cancelled.
 */
suspend inline fun <T> Chunk.launchWithTicket(crossinline block: suspend (Chunk) -> T): Deferred<T> {
    val deferred = CompletableDeferred<T>()
//    gearyPaper.plugin.launch {
    try {
        addPluginChunkTicket(gearyPaper.plugin)
        deferred.complete(block(this@launchWithTicket))
    } catch (e: Exception) {
        deferred.completeExceptionally(e)
    } finally {
        removePluginChunkTicket(gearyPaper.plugin)
    }
//    }
    return deferred
}
