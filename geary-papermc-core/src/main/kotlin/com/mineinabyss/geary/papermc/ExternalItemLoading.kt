package com.mineinabyss.geary.papermc

import java.util.concurrent.CompletableFuture

object ExternalItemLoading {

    private var loaded: CompletableFuture<Void?> = CompletableFuture.completedFuture(null)

    fun awaitLoaded(): CompletableFuture<Void?> = loaded

    fun started() {
        if (loaded.isDone) loaded = CompletableFuture()
    }

    fun finished() {
        loaded.complete(null)
    }
}
