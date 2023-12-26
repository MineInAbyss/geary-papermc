package com.mineinabyss.geary.papermc.bridge.systems.apply

import com.mineinabyss.geary.papermc.bridge.actions.components.ApplicableAttribute
import com.mineinabyss.geary.papermc.configlang.components.Apply
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.LivingEntity

/**
 * Modifies a specified attribute by a specifies amount.
 */
class ApplyAttribute : GearyListener() {
    val Pointers.bukkit by get<BukkitEntity>().on(target)
    val Pointers.attributes by getRelationsWithData<Apply?, ApplicableAttribute>().on(event)

    override fun Pointers.handle() {
        attributes.forEach { attribute ->
            val living: LivingEntity = bukkit as? LivingEntity ?: return
            val (attribute, amplifier) = attribute.targetData
            living.getAttribute(attribute)?.baseValue = amplifier
        }
    }
}
