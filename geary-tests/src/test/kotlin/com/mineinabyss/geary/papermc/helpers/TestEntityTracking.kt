package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.EntityTrackingModule
import com.mineinabyss.geary.papermc.tracking.items.BukkitBackedItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.get
import org.kodein.di.bindSingleton
import org.kodein.di.bindSingletonOf
import org.kodein.di.delegate

val TestEntityTracking: Feature<EntityTrackingModule> = EntityTracking.overrideScope {
    bindSingleton { BukkitEntity2Geary(forceMainThread = false, get(), get()) }
}

val TestItemTracking = ItemTracking.overrideScope {
    bindSingletonOf(::BukkitBackedItemTracking)
    delegate<ItemTrackingModule>().to<BukkitBackedItemTracking>()
}