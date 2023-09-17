package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.HealthConditions
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

class HealthConditionChecker : CheckingListener() {
    private val Pointers.bukkit by get<BukkitEntity>().on(target)
    private val Pointers.health by get<HealthConditions>().on(event)

    override fun Pointers.check(): Boolean {
        val living = bukkit as? LivingEntity ?: return false

        return (health.within nullOr { living.health in it } && health.withinPercent nullOr {
            living.health / (living.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return false) in it
        })
    }
}
