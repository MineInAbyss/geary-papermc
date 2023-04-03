package com.mineinabyss.geary.papermc.tracking.items.migration

import com.mineinabyss.geary.papermc.datastore.encodePrefabs
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import org.bukkit.persistence.PersistentDataContainer

class ItemMigration {
    val map = mutableMapOf<Int, PrefabKey>()

    fun encodePrefabsFromCustomModelData(pdc: PersistentDataContainer, item: NMSItemStack): Boolean {
        if (!gearyPaper.config.migrateItemCustomModelDataToPrefab) return false
        val tag = item.tag ?: return false
        if (!tag.contains("CustomModelData")) return false
        val prefab = map[tag.getInt("CustomModelData")] ?: return false
        pdc.encodePrefabs(listOf(prefab))
        return true
    }
}
