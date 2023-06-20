package com.mineinabyss.geary.papermc.bridge.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:event.left_clicked")
sealed class LeftClicked

@Serializable
@SerialName("geary:event.right_clicked")
sealed class RightClicked

@Serializable
@SerialName("geary:event.item_interacted")
sealed class Interacted

sealed class ItemBroke

sealed class ItemDropped
