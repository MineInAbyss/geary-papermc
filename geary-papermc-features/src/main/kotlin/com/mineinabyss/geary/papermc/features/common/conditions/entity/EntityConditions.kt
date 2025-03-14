package com.mineinabyss.geary.papermc.features.common.conditions.entity

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Conditions that can be checked against a [LivingEntity].
 *
 * This does not count [Player], for that use [PlayerConditions](PlayerConditions.kt)
 */
@Serializable
@SerialName("geary:check.entity")
class EntityConditions(
    val sleeping: Boolean? = null,
    val swimming: Boolean? = null,
    val climbing: Boolean? = null,
    val jumping: Boolean? = null,
    val inLava: Boolean? = null,
    val inWater: Boolean? = null,
    val inBubbleColumn: Boolean? = null,
    val inRain: Boolean? = null,
    val onGround: Boolean? = null,
    val gliding: Boolean? = null,
    val frozen: Boolean? = null,
    val inPowderedSnow: Boolean? = null,
    val inCobweb: Boolean? = null,
    val insideVehicle: Boolean? = null,
    val riptiding: Boolean? = null,
    val invisible: Boolean? = null,
    val glowing: Boolean? = null,
    val invurnerable: Boolean? = null,
    val silent: Boolean? = null,
    val leashed: Boolean? = null,
    val health: @Serializable(with= DoubleRangeSerializer::class) DoubleRange? = null,
) : Condition {
    infix fun Boolean?.nullOrEquals(other: Boolean) = this == null || this == other

    override fun ActionGroupContext.execute(): Boolean {
        val livingEntity = entity?.get<LivingEntity>() ?: return false
        return sleeping nullOrEquals livingEntity.isSleeping &&
                swimming nullOrEquals livingEntity.isSwimming &&
                climbing nullOrEquals livingEntity.isClimbing &&
                jumping nullOrEquals livingEntity.isJumping &&
                inLava nullOrEquals livingEntity.isInLava &&
                inWater nullOrEquals livingEntity.isInWater &&
                inBubbleColumn nullOrEquals livingEntity.isInBubbleColumn &&
                inRain nullOrEquals livingEntity.isInRain &&
                onGround nullOrEquals livingEntity.isOnGround &&
                gliding nullOrEquals livingEntity.isGliding &&
                frozen nullOrEquals livingEntity.isFrozen &&
                inPowderedSnow nullOrEquals livingEntity.isInPowderedSnow &&
                inCobweb nullOrEquals (livingEntity.location.block.type == org.bukkit.Material.COBWEB) &&
                insideVehicle nullOrEquals livingEntity.isInsideVehicle &&
                riptiding nullOrEquals livingEntity.isRiptiding &&
                invisible nullOrEquals livingEntity.isInvisible &&
                glowing nullOrEquals livingEntity.isGlowing &&
                invurnerable nullOrEquals livingEntity.isInvulnerable &&
                silent nullOrEquals livingEntity.isSilent &&
                leashed nullOrEquals livingEntity.isLeashed &&
                (health == null || livingEntity.health in health)
    }
}
