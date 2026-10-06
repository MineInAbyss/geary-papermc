package com.mineinabyss.geary.papermc.features.items.posteffects

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.idofront.features.listeners
import org.bukkit.Bukkit

val PostEffectsFeature = module("post-effects") {
    gearyWorld { syncPostEffectsSystem() }
    listeners(PostEffectsListener())
    // A reload stops the sync system, without this players would keep rendering whatever was last applied
    addCloseable { Bukkit.getOnlinePlayers().forEach(PlayerPostEffects::clear) }
}
