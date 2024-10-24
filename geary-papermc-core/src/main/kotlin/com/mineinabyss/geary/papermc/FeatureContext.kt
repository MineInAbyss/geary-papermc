package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.Plugin

data class FeatureContext(
    val plugin: Plugin,
    val logger: ComponentLogger,
    val isFirstEnable: Boolean,
)
