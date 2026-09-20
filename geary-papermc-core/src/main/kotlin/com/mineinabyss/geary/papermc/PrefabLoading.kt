package com.mineinabyss.geary.papermc

import java.util.concurrent.CompletableFuture

/**
 * Tracks whether prefabs are still being read, for plugins that must not act on a half-loaded set.
 *
 * Prefabs load a tick after the plugin enables and take a while to walk every namespace, so anything a
 * prefab registers with another plugin lands well after that plugin started. Nexo for one builds its
 * resourcepack and fires its loaded-event on its own first tick, leaving prefab-declared items out of
 * both until something reloads.
 *
 * Starts completed, so a server not loading prefabs at all never leaves a waiter hanging.
 */
object PrefabLoading {

    private var loaded: CompletableFuture<Void?> = CompletableFuture.completedFuture(null)

    /** Completes once the prefabs currently being read are all loaded */
    fun awaitLoaded(): CompletableFuture<Void?> = loaded

    /** Marks a load as in-flight, called again for every reload */
    fun started() {
        if (loaded.isDone) loaded = CompletableFuture()
    }

    fun finished() {
        loaded.complete(null)
    }
}
