package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.addons.install
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.withUUIDSerializer
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.items.BukkitBackedItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.dsl.serialization

fun GearyModule.withMockTracking(
    entities: Boolean = true,
    items: Boolean = true,
// TODO    blocks: Boolean = true,
) {
    serialization {
        withUUIDSerializer()
        withTestSerializers()
    }
    if (entities) install(EntityTracking, TestEntityTrackingConfiguration)
    if (items) install(ItemTracking) {
        module = ::BukkitBackedItemTracking
    }
    install(Prefabs)
    geary.pipeline.runStartupTasks()
}
