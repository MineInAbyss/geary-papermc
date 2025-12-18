package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule
import com.mineinabyss.geary.papermc.tracking.items.BukkitBackedItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.idofront.features.Feature
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind

val TestEntityTracking: Feature<EntityTrackingModule> = EntityTracking.overrideScope {
    scoped { BukkitEntity2Geary(forceMainThread = false, get(), get()) }
}

val TestItemTracking = ItemTracking.overrideScope {
    scopedOf(::BukkitBackedItemTracking) bind ItemTrackingModule::class
}