package com.mineinabyss.geary.papermc.tracking.blocks.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.loadComponentsFrom
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.papermc.tracking.blocks.gearyBlocks
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class TrackOnSetBlockComponent : GearyListener() {
    private val TargetScope.block by onSet<SetBlock>()

    @Handler
    fun TargetScope.loadEntity() {
        gearyBlocks.block2Prefab[gearyBlocks.blockMap.]
        gearyBlocks.bukkit2Geary[bukkit] = entity

        // Load persisted components
        val pdc = bukkit.persistentDataContainer
        if (pdc.hasComponentsEncoded)
            entity.loadComponentsFrom(pdc)

        // allow us to both get the BukkitEntity and specific class (ex Player)
        bukkit.type.entityClass?.kotlin?.let { bukkitClass ->
            entity.set(bukkit, bukkitClass)
        }

        entity.set(bukkit.uniqueId)
    }
}
