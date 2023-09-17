package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.EntityConditions
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class EntityConditionsChecker : CheckingListener() {
    private val Pointers.livingEntity by get<LivingEntity>().on(target)

    private val Pointers.conditions by get<EntityConditions>().on(event)

    override fun Pointers.check(): Boolean = livingEntity !is Player &&
            conditions.isSleeping nullOrEquals livingEntity.isSleeping &&
            conditions.isSwimming nullOrEquals livingEntity.isSwimming &&
            conditions.isClimbing nullOrEquals livingEntity.isClimbing &&
            conditions.isJumping nullOrEquals livingEntity.isJumping &&
            conditions.isInLava nullOrEquals livingEntity.isInLava &&
            conditions.isInWater nullOrEquals livingEntity.isInWater &&
            conditions.isInBubbleColumn nullOrEquals livingEntity.isInBubbleColumn &&
            conditions.isInRain nullOrEquals livingEntity.isInRain &&
            conditions.isOnGround nullOrEquals livingEntity.isOnGround &&
            conditions.isGliding nullOrEquals livingEntity.isGliding &&
            conditions.isFrozen nullOrEquals livingEntity.isFrozen &&
            conditions.isInPowderedSnow nullOrEquals livingEntity.isInPowderedSnow &&
            conditions.isInCobweb nullOrEquals (livingEntity.location.block.type == Material.COBWEB) &&
            conditions.isInsideVehicle nullOrEquals livingEntity.isInsideVehicle &&
            conditions.isRiptiding nullOrEquals livingEntity.isRiptiding &&
            conditions.isInvisible nullOrEquals livingEntity.isInvisible &&
            conditions.isGlowing nullOrEquals livingEntity.isGlowing &&
            conditions.isInvurnerable nullOrEquals livingEntity.isInvulnerable &&
            conditions.isSilent nullOrEquals livingEntity.isSilent &&
            conditions.isLeashed nullOrEquals livingEntity.isLeashed

}
