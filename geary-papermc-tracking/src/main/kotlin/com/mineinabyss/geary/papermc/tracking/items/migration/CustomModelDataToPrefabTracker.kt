package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createCustomModelDataToPrefabTracker() = listener(
    object : ListenerQuery() {
        val key by get<PrefabKey>()
        val item by get<SetItem>()
        override fun ensure() = event.anySet(::key, ::item)
    }
).exec {
    val customModelData = item.item.customModelData ?: return@exec
    gearyItems.migration.map[customModelData] = key
}
