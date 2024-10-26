package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.tracking.items.BukkitBackedItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.serialization

fun GearySetup.withMockTracking(
    entities: Boolean = true,
    items: Boolean = true,
// TODO    blocks: Boolean = true,
) {
    serialization {
        withUUIDSerializer()
        withTestSerializers()
    }
    if (entities) install(TestEntityTracking)
    if (items) install(ItemTracking.withConfig { BukkitBackedItemTracking(this) })
    install(Prefabs)
    geary.pipeline.runStartupTasks()
}
