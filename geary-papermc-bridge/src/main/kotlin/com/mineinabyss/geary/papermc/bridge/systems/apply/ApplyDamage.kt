package com.mineinabyss.geary.papermc.bridge.systems.apply

import com.mineinabyss.geary.papermc.bridge.actions.components.DealDamage
import com.mineinabyss.geary.papermc.configlang.components.Apply
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.randomOrMin
import org.bukkit.entity.LivingEntity

class ApplyDamage : GearyListener() {
    val Pointers.bukkit by get<BukkitEntity>().on(target)
    val Pointers.damageRelations by getRelationsWithData<Apply?, DealDamage>().on(event)
    override fun Pointers.handle() {
        val livingTarget = bukkit as? LivingEntity ?: return

        damageRelations.forEach { damageRelation ->
            with(damageRelation.targetData) {
                val chosenDamage = damage.randomOrMin()
                //if true, damage dealt ignores armor, otherwise factors armor into damage calc
                livingTarget.health = (livingTarget.health - chosenDamage).coerceAtLeast(minHealth)
            }
        }
    }
}
