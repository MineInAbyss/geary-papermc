package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.conditions.HealthConditions
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

class HealthConditionChecker : GearyListener() {
    private val TargetScope.bukkit by get<BukkitEntity>()
    private val TargetScope.health by get<HealthConditions>()

    @Handler
    fun TargetScope.checkHealth(): Boolean {
        val living = bukkit as? LivingEntity ?: return false

        return (health.within nullOr { living.health in it }
                && health.withinPercent nullOr {
            living.health / (living.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return false) in it
        })
    }
}
