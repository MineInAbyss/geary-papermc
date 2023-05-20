package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.EntityConditions
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class EntityConditionsChecker : GearyListener() {
    private val TargetScope.livingEntity by get<LivingEntity>()
    private val TargetScope.conditions by get<EntityConditions>()

    @Handler
    fun TargetScope.check(): Boolean = livingEntity !is Player &&
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
