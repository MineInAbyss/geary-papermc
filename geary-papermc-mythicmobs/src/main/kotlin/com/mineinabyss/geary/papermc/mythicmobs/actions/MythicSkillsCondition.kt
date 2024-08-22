package com.mineinabyss.geary.papermc.mythicmobs.actions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.api.skills.conditions.ILocationCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

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

    override fun ActionGroupContext.execute(): Boolean {
        val entity = BukkitAdapter.adapt(entity.get<BukkitEntity>())
        val location = entity?.location ?: BukkitAdapter.adapt(location)
        return keys.all { line ->
            val condition = MythicBukkit.inst().skillManager.getCondition(line)
            if (condition is ILocationCondition)
                condition.check(location)
            else
                condition.evaluateEntity(entity)
        }
    }
}
