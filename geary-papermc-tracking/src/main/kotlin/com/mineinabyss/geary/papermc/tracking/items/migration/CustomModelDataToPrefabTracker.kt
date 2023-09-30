package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class CustomModelDataToPrefabTracker : GearyListener() {
    private val Pointers.key by get<PrefabKey>().whenSetOnTarget()
    private val Pointers.item by get<SetItem>().whenSetOnTarget()

    override fun Pointers.handle() {
        val customModelData = item.item.customModelData ?: return
        gearyItems.migration.map[customModelData] = key
    }
}
