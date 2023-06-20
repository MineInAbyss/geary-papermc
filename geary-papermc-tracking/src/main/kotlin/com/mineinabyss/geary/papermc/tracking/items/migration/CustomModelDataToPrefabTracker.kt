package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class CustomModelDataToPrefabTracker : GearyListener() {
    private val TargetScope.key by onSet<PrefabKey>()
    private val TargetScope.item by onSet<SetItem>()

    @Handler
    fun TargetScope.handle() {
        val customModelData = item.item.customModelData ?: return
        gearyItems.migration.map[customModelData] = key
    }
}
