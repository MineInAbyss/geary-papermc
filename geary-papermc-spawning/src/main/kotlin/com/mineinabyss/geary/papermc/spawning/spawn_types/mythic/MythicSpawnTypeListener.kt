package com.mineinabyss.geary.papermc.spawning.spawn_types.mythic

import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadEntityTypeEvent
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadSpawnCategoryEvent
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadTypeEvent
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class MythicSpawnTypeListener : Listener {
    val mobsManager = MythicBukkit.inst().mobManager
    val mobSpawnCategoryCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1.minutes.toJavaDuration())
        .build<String, String>()

    @EventHandler
    fun GearyReadTypeEvent.readMythicType() {
        if (spawnType != null) return

        val key = PrefabKey.ofOrNull(name) ?: return

        if (key.namespace.startsWith("mm") || key.namespace.startsWith("mythic")) {
            spawnType = MythicSpawnType(name, key.key)
        }
    }

    @EventHandler
    fun GearyReadSpawnCategoryEvent.readSpawnCategory() {
        if (category != null) return
        val mob = mobsManager.getActiveMob(entity.uniqueId).getOrNull() ?: return
        // MM's config string reading is super slow, so cache it
        val category = mobSpawnCategoryCache.get(mob.mobType) {
            mob.type.config.getString("SpawnCategory") ?: "default"
        }
        this.category = SpawnCategory(category)
    }

    @EventHandler
    fun GearyReadEntityTypeEvent.readEntityType() {
        if (type != null) return
        val typeName = mobsManager.getActiveMob(entity.uniqueId).getOrNull()?.mobType ?: return
        type = "mm:$typeName"
    }
}
