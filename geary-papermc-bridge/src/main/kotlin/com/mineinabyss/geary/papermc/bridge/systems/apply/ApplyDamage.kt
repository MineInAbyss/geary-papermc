package com.mineinabyss.geary.papermc.bridge.systems.apply

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.actions.components.DealDamage
import com.mineinabyss.geary.papermc.commons.events.configurable.components.Apply
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.entity.LivingEntity

class ApplyDamage : GearyListener() {
    val TargetScope.bukkit by get<BukkitEntity>()
    val EventScope.damage by getRelations<Apply?, DealDamage>()

    @Handler
    fun applyDamage(target: TargetScope, event: EventScope) {
        val livingTarget = target.bukkit as? LivingEntity ?: return

        with(event.damage.targetData) {
            val chosenDamage = damage.randomOrMin()
            //if true, damage dealt ignores armor, otherwise factors armor into damage calc
            livingTarget.health = (livingTarget.health - chosenDamage).coerceAtLeast(minHealth)
        }
    }
}
