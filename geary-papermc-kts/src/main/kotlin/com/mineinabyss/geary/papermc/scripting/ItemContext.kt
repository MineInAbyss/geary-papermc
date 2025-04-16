package com.mineinabyss.geary.papermc.scripting

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.scripting.builders.ParticleBuilder
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

data class ItemContext(
    val item: GearyEntity,
    /** The player or mob holding this item. */
    val itemHolder: LivingEntity,
    override val coroutineContext: CoroutineContext,
): CoroutineScope {
    fun explode(location: Location, power: Number) {
        location.createExplosion(power.toFloat())
    }

    fun cooldown(text: String, duration: Duration) {
    }

    fun cooldown(duration: Duration) {
    }

    fun mythicSkills(caster: LivingEntity, vararg skills: String) {

    }
}
