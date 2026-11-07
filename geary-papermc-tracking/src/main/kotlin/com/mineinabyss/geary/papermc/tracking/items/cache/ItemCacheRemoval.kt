package com.mineinabyss.geary.papermc.tracking.items.cache

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnEntityRemoved
import com.mineinabyss.geary.observers.events.OnRemove
import com.mineinabyss.geary.systems.query.query

fun WorldScoped.clearItemCachesOnRemoval() {
    // Entity removal removes children (the cached item entities) before it emits OnRemove, so the holder's cache must be cleared on OnEntityRemoved, which runs first
    observe<OnEntityRemoved>().exec(query<PlayerItemCache<*>>()) { (cache) ->
        cache.clear()
    }
    observe<OnRemove>().involving(query<PlayerItemCache<*>>()).exec { (cache) ->
        cache.clear()
    }
}
