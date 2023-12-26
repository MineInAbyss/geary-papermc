package com.mineinabyss.geary.papermc.bridge.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * `geary:cancel_bukkit_event`
 * Cancels any bukkit event added to an event entity when added to that same entity.
 *
 * TODO implement
 */
@Serializable
@SerialName("geary:cancel_bukkit_event")
sealed class CancelBukkitEvent
