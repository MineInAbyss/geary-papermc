package com.mineinabyss.geary.papermc.mythicmobs.actions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.api.skills.conditions.ILocationCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicReloadedEvent
import io.lumine.mythic.core.skills.SkillCondition
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@Serializable(with = MythicSkillsCondition.Serializer::class)
class MythicSkillsCondition(
    val keys: List<String>,
) : Condition {
    class Serializer : InnerSerializer<List<String>, MythicSkillsCondition>(
        serialName = "geary:mythic_conditions",
        inner = ListSerializer(String.serializer()),
        inverseTransform = { it.keys },
        transform = { MythicSkillsCondition(it) }
    )

    private val parsed = arrayOfNulls<SkillCondition>(keys.size)
    private var parsedGeneration = MythicConditionReloads.generation

    override fun ActionGroupContext.execute(): Boolean {
        val entity = BukkitAdapter.adapt(getOrNull<GearyEntity>("entity")?.get<BukkitEntity>())
        val location = entity?.location ?: BukkitAdapter.adapt(location)
        if (parsedGeneration != MythicConditionReloads.generation) {
            parsed.fill(null)
            parsedGeneration = MythicConditionReloads.generation
        }
        return keys.indices.all { index ->
            val condition = parsed[index]
                ?: MythicBukkit.inst().skillManager.getCondition(keys[index]).also { parsed[index] = it }
            if (condition is ILocationCondition)
                condition.check(location)
            else
                condition.evaluateEntity(entity)
        }
    }
}

object MythicConditionReloads : Listener {
    var generation = 0
        private set

    @EventHandler
    fun MythicReloadedEvent.onMythicReload() {
        generation++
    }
}

fun <T> ActionGroupContext.getOrNull(key: String): T? = environment[key] as? T
