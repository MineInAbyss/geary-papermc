package com.mineinabyss.geary.papermc.plugin.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@SerialName("geary:debug_component")
data class DebugComponent(val time: Long = Instant.now().toEpochMilli())
