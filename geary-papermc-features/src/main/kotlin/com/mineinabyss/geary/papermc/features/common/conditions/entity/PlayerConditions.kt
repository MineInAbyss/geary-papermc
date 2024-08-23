package com.mineinabyss.geary.papermc.features.common.conditions.entity

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:player")
class PlayerConditions(
    val sneaking: Boolean? = null,
    val sprinting: Boolean? = null,
    val blocking: Boolean? = null,
    val sleeping: Boolean? = null,
    val deeplySleeping: Boolean? = null,
    val swimming: Boolean? = null,
    val climbing: Boolean? = null,
    val jumping: Boolean? = null,
    val inLava: Boolean? = null,
    val inWater: Boolean? = null,
    val inBubbleColumn: Boolean? = null,
    val inRain: Boolean? = null,
    val flying: Boolean? = null,
    val gliding: Boolean? = null,
    val frozen: Boolean? = null,
    val freezeTickingLocked: Boolean? = null,
    val inPowderedSnow: Boolean? = null,
    val inCobweb: Boolean? = null,
    val insideVehicle: Boolean? = null,
    val conversing: Boolean? = null,
    val riptiding: Boolean? = null,
    val invisible: Boolean? = null,
    val glowing: Boolean? = null,
    val invurnerable: Boolean? = null,
    val silent: Boolean? = null,
    val op: Boolean? = null,
) : Condition {
    infix fun Boolean?.nullOrEquals(other: Boolean) = this == null || this == other
    override fun ActionGroupContext.execute(): Boolean {
        val player = entity?.get<Player>() ?: return false
        return player.isOnline && // Just to align syntax below
                sneaking nullOrEquals player.isSneaking &&
                sprinting nullOrEquals player.isSprinting &&
                blocking nullOrEquals player.isBlocking &&
                sleeping nullOrEquals player.isSleeping &&
                deeplySleeping nullOrEquals player.isDeeplySleeping &&
                swimming nullOrEquals player.isSwimming &&
                climbing nullOrEquals player.isClimbing &&
                jumping nullOrEquals player.isJumping &&
                inLava nullOrEquals player.isInLava &&
                inWater nullOrEquals player.isInWater &&
                inBubbleColumn nullOrEquals player.isInBubbleColumn &&
                inRain nullOrEquals player.isInRain &&
                flying nullOrEquals player.isFlying &&
                gliding nullOrEquals player.isGliding &&
                frozen nullOrEquals player.isFrozen &&
                freezeTickingLocked nullOrEquals player.isFreezeTickingLocked &&
                inPowderedSnow nullOrEquals player.isInPowderedSnow &&
                inCobweb nullOrEquals (player.location.block.type == org.bukkit.Material.COBWEB) &&
                insideVehicle nullOrEquals player.isInsideVehicle &&
                conversing nullOrEquals player.isConversing &&
                riptiding nullOrEquals player.isRiptiding &&
                invisible nullOrEquals player.isInvisible &&
                glowing nullOrEquals player.isGlowing &&
                invurnerable nullOrEquals player.isInvulnerable &&
                silent nullOrEquals player.isSilent &&
                op nullOrEquals player.isOp
    }
}
