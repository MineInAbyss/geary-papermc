package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.prefabs.configuration.components.Condition
import com.mineinabyss.geary.prefabs.configuration.components.RoleContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:player")
class PlayerConditions(
    val isSneaking: Boolean? = null,
    val isSprinting: Boolean? = null,
    val isBlocking: Boolean? = null,
    val isSleeping: Boolean? = null,
    val isDeeplySleeping: Boolean? = null,
    val isSwimming: Boolean? = null,
    val isClimbing: Boolean? = null,
    val isJumping: Boolean? = null,
    val isInLava: Boolean? = null,
    val isInWater: Boolean? = null,
    val isInBubbleColumn: Boolean? = null,
    val isInRain: Boolean? = null,
    val isFlying: Boolean? = null,
    val isGliding: Boolean? = null,
    val isFrozen: Boolean? = null,
    val isFreezeTickingLocked: Boolean? = null,
    val isInPowderedSnow: Boolean? = null,
    val isInCobweb: Boolean? = null,
    val isInsideVehicle: Boolean? = null,
    val isConversing: Boolean? = null,
    val isRiptiding: Boolean? = null,
    val isInvisible: Boolean? = null,
    val isGlowing: Boolean? = null,
    val isInvurnerable: Boolean? = null,
    val isSilent: Boolean? = null,
    val isOp: Boolean? = null,
) : Condition {
    infix fun Boolean?.nullOrEquals(other: Boolean) = this == null || this == other

    override fun RoleContext.execute(): Boolean {
        val player = entity.get<Player>() ?: return false
        return player.isOnline && // Just to align syntax below
                isSneaking nullOrEquals player.isSneaking &&
                isSprinting nullOrEquals player.isSprinting &&
                isBlocking nullOrEquals player.isBlocking &&
                isSleeping nullOrEquals player.isSleeping &&
                isDeeplySleeping nullOrEquals player.isDeeplySleeping &&
                isSwimming nullOrEquals player.isSwimming &&
                isClimbing nullOrEquals player.isClimbing &&
                isJumping nullOrEquals player.isJumping &&
                isInLava nullOrEquals player.isInLava &&
                isInWater nullOrEquals player.isInWater &&
                isInBubbleColumn nullOrEquals player.isInBubbleColumn &&
                isInRain nullOrEquals player.isInRain &&
                isFlying nullOrEquals player.isFlying &&
                isGliding nullOrEquals player.isGliding &&
                isFrozen nullOrEquals player.isFrozen &&
                isFreezeTickingLocked nullOrEquals player.isFreezeTickingLocked &&
                isInPowderedSnow nullOrEquals player.isInPowderedSnow &&
                isInCobweb nullOrEquals (player.location.block.type == org.bukkit.Material.COBWEB) &&
                isInsideVehicle nullOrEquals player.isInsideVehicle &&
                isConversing nullOrEquals player.isConversing &&
                isRiptiding nullOrEquals player.isRiptiding &&
                isInvisible nullOrEquals player.isInvisible &&
                isGlowing nullOrEquals player.isGlowing &&
                isInvurnerable nullOrEquals player.isInvulnerable &&
                isSilent nullOrEquals player.isSilent &&
                isOp nullOrEquals player.isOp
    }
}
