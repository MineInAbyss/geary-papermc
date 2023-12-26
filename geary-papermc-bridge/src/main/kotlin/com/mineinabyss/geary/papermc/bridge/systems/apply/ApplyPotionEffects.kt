package com.mineinabyss.geary.papermc.bridge.systems.apply

import com.mineinabyss.geary.papermc.bridge.actions.components.PotionEffects
import com.mineinabyss.geary.papermc.configlang.components.Apply
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.LivingEntity

/**
 * Handles being exposed to potion effects
 */
class ApplyPotionEffects : GearyListener() {
    val Pointers.bukkit by get<BukkitEntity>().on(target)
    val Pointers.exposedEffectRelations by getRelationsWithData<Apply?, PotionEffects>().on(event)

    override fun Pointers.handle() {
        exposedEffectRelations.forEach { exposedEffects ->
            (bukkit as? LivingEntity)?.addPotionEffects(exposedEffects.targetData.effects)
        }
    }
}
