package com.mineinabyss.geary.papermc.creativetabs

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.messaging.ComponentLogger

/** Sends prefab blocks, furniture and other items as creative tabs to players running the MineInAbyss Addon mod */
val CreativeTabsFeature = module("creative-tabs") {
    requirePlugins("Nexo")

    val channel = CreativeTabsChannel(get(), get<ComponentLogger>())
    channel.open()
    addCloseable { channel.close() }
    listeners(CreativeTabsListener(channel))
    channel.rebuildOnceLoaded()
}
