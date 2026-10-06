package com.mineinabyss.geary.papermc.features.items.lightsource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sent as a client-side `minecraft:light` block so the client's light engine lights the area
 *
 * [forwardOffset] is blocks ahead of the eyes to place the light, stopping at the first non-air block.
 * A light block cannot be aimed, so this is the only way to light less behind the player.
 * Zero keeps it on the player, which skips the raycast when they only look around
 * */
@Serializable
@SerialName("geary:light_source")
data class LightSource(val level: Int = 11, val forwardOffset: Double = 0.0) {
    init {
        require(level in 1..15) { "LightSource level must be between 1 and 15" }
        require(forwardOffset >= 0.0) { "LightSource forwardOffset must not be negative" }
    }
}
