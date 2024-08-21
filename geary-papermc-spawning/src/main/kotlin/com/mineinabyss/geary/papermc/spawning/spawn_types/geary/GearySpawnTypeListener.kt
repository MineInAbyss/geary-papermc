package com.mineinabyss.geary.papermc.spawning.spawn_types.geary

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadSpawnCategoryEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GearySpawnTypeListener : Listener {
    @EventHandler
    fun GearyReadSpawnCategoryEvent.readSpawnCategory() {
        if (category != null) return
        val cat = entity.toGearyOrNull()?.get<SpawnCategory>() ?: return
        category = cat
    }
}

