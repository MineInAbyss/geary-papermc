package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.DoubleRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.LivingEntity

/**
 * Deals damage to the target entity.
 *
 * @param damage The damage amount.
 */
@Serializable
@SerialName("geary:damage")
data class DoDamage(
    val damage: @Serializable(with = DoubleRangeSerializer::class) DoubleRange,
    val minHealth: Double = 0.0,
    val ignoreArmor: Boolean = false,
)


class DoDamageSystem : GearyListener() {
    val Pointers.bukkit by get<BukkitEntity>().on(target)
    val Pointers.damage by get<DoDamage>().on(source)

    override fun Pointers.handle() {
        val living = bukkit as? LivingEntity ?: return
        if (living.health > damage.minHealth) {
            if (damage.ignoreArmor) {
                living.health -= damage.damage.randomOrMin()
            } else {
                living.damage(damage.damage.randomOrMin())
            }
        }
    }
}
