package com.mineinabyss.geary.papermc

import co.touchlab.kermit.Logger
import org.bukkit.plugin.Plugin

data class FeatureContext(
    val plugin: Plugin,
    val logger: Logger,
)
