package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.dependencies.and
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.geary.papermc.tracking.entities.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.entities.MCEntityTracking
import com.mineinabyss.geary.papermc.tracking.items.BukkitBackedItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.geary.papermc.tracking.items.MCItemTracking

val TestEntityTracking = MCEntityTracking.override {
    gearyWorld {
        world.install(EntityTracking.override {
            single(ignoreOverride = true) { BukkitEntity2Geary(forceMainThread = false, get(), get()) }
        })
    }
}

val TestItemTracking = MCItemTracking.override {
    gearyWorld {
        world.install(ItemTracking.override {
            single(ignoreOverride = true) { new(::BukkitBackedItemTracking) }.and<ItemTrackingModule>()
        })
    }
}