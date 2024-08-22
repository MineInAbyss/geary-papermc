package com.mineinabyss.geary.papermc.spawning.spawn_types.mythic

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadSpawnCategoryEvent
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadTypeEvent
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicSpawnTypeListener : Listener {
    val mobsManager = MythicBukkit.inst().mobManager

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

        if (mobsManager.isMythicMob(entity)) {
            val cat = mobsManager.getMythicMobInstance(entity)?.type?.config?.getString("SpawnCategory") ?: return
            category = SpawnCategory(cat)
        }
    }
}

