package com.mineinabyss.geary.papermc.mythicmobs

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import io.lumine.mythic.api.mobs.MythicMob
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.ActiveMob

object GearyMythicConfigOptions {
    val MythicMob.prefabs
        get() = config.getStringList("Prefabs")
            .mapNotNull { PrefabKey.ofOrNull(it) }

    fun ActiveMob.addPrefabs(prefabs: List<PrefabKey>) {
        if (prefabs.isEmpty()) return
        val bukkit = BukkitAdapter.adapt(entity)
        with(bukkit.world.toGeary()) {
            val gearyMob = bukkit.toGeary()
            gearyPaper.plugin.launch {
                prefabs.mapNotNull { entityOfOrNull(it) }
                    .forEach(gearyMob::extend)
            }
        }
    }
}
