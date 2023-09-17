package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.PlayerConditions
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Material
import org.bukkit.entity.Player

class PlayerConditionsChecker : CheckingListener() {
    private val Pointers.player by get<Player>().on(target)
    private val Pointers.conditions by get<PlayerConditions>().on(event)

    override fun Pointers.check(): Boolean = player.isOnline && // Just to align syntax below
            conditions.isSneaking nullOrEquals player.isSneaking &&
            conditions.isSprinting nullOrEquals player.isSprinting &&
            conditions.isBlocking nullOrEquals player.isBlocking &&
            conditions.isSleeping nullOrEquals player.isSleeping &&
            conditions.isDeeplySleeping nullOrEquals player.isDeeplySleeping &&
            conditions.isSwimming nullOrEquals player.isSwimming &&
            conditions.isClimbing nullOrEquals player.isClimbing &&
            conditions.isJumping nullOrEquals player.isJumping &&
            conditions.isInLava nullOrEquals player.isInLava &&
            conditions.isInWater nullOrEquals player.isInWater &&
            conditions.isInBubbleColumn nullOrEquals player.isInBubbleColumn &&
            conditions.isInRain nullOrEquals player.isInRain &&
            conditions.isFlying nullOrEquals player.isFlying &&
            conditions.isGliding nullOrEquals player.isGliding &&
            conditions.isFrozen nullOrEquals player.isFrozen &&
            conditions.isFreezeTickingLocked nullOrEquals player.isFreezeTickingLocked &&
            conditions.isInPowderedSnow nullOrEquals player.isInPowderedSnow &&
            conditions.isInCobweb nullOrEquals (player.location.block.type == Material.COBWEB) &&
            conditions.isInsideVehicle nullOrEquals player.isInsideVehicle &&
            conditions.isConversing nullOrEquals player.isConversing &&
            conditions.isRiptiding nullOrEquals player.isRiptiding &&
            conditions.isInvisible nullOrEquals player.isInvisible &&
            conditions.isGlowing nullOrEquals player.isGlowing &&
            conditions.isInvurnerable nullOrEquals player.isInvulnerable &&
            conditions.isSilent nullOrEquals player.isSilent &&
            conditions.isOp nullOrEquals player.isOp
}
