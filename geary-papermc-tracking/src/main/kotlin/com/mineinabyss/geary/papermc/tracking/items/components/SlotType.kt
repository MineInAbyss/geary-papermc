package com.mineinabyss.geary.papermc.tracking.items.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class SlotType {
    sealed class Held

    sealed class Offhand

    sealed class Hotbar

    sealed class Equipped
}
