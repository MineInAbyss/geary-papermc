package com.mineinabyss.geary.papermc.features.items.lightsource

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.idofront.features.listeners
import org.bukkit.Bukkit

val LightSourceFeature = module("light-source") {
    gearyWorld { syncLightSourceSystem() }
    listeners(LightSourceListener())
    // A reload stops the sync system, without this players would be left with a light block that never moves again
    addCloseable { Bukkit.getOnlinePlayers().forEach(PlayerLightBlocks::release) }
}
