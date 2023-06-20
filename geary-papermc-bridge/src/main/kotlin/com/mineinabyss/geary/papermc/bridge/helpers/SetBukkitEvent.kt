package com.mineinabyss.geary.papermc.bridge.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.event.Event

fun GearyEntity.setBukkitEvent(event: Event) {
    // Set both the direct class and generic Event
    set(event, event::class)
    set(event)
}
