package com.mineinabyss.geary.papermc.spawning.conditions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadEntityTypeEvent
import com.mineinabyss.idofront.events.call
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:max_nearby")
class NearbyCondition(
    val amount: Int,
    val types: Expression<List<String>> = Expression.Variable("spawnTypes"),
    val radius: Double = gearyPaper.features.getOrNull<SpawningFeature>()?.config?.range?.defaultNearbyRange ?: 128.0,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val types = eval(types)
        val location = location ?: return true
        return location.world.getNearbyEntities(location, radius, radius, radius) {
            GearyReadEntityTypeEvent(it).apply { call() }.type in types
        }.size < amount
    }
}
