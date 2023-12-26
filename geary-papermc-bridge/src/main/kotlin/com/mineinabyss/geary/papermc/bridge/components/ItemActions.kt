package com.mineinabyss.geary.papermc.bridge.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:event.leftClicked")
sealed class LeftClicked

@Serializable
@SerialName("geary:event.rightClicked")
sealed class RightClicked

@Serializable
@SerialName("geary:event.itemInteracted")
sealed class Interacted

sealed class ItemBroke

sealed class ItemDropped
