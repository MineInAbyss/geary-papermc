package com.mineinabyss.geary.papermc.features.entities.pathfinders

import com.charleskorn.kaml.Yaml
import com.destroystokyo.paper.entity.ai.PaperGoal
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.nms.aliases.NMSMob
import com.mineinabyss.idofront.nms.aliases.toNMS
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import net.minecraft.world.entity.ai.goal.Goal
import org.bukkit.entity.Mob
import java.lang.reflect.Constructor
import java.lang.reflect.Parameter

@Serializable(with = PathfinderWrapper.Serializer::class)
data class PathfinderWrapper(
    val type: String,
    val parameters: Map<String, String>,
    val priority: Int,
) {
    @Transient
    private val candidates = Class.forName(PATHFINDER_PACKAGE + type).constructors.filter {
        it.parameterCount == (parameters.size + 1) && NMSMob::class.java.isAssignableFrom(it.parameters.first().type)
    }

    @Transient
    val constructor: Constructor<*> = candidates.firstOrNull { it.goalParameters.map { it.name }.containsAll(parameters.keys) }
        ?: candidates.singleOrNull { it.goalParameters.none(Parameter::isNamePresent) }
        ?: error("Could not find constructor for pathfinder $type, has the class name changed?")

    @Transient
    val decoded = constructor.goalParameters.mapIndexed { index, param ->
        val value = if (param.isNamePresent) parameters[param.name] ?: error("Missing parameter ${param.name}")
        else parameters.values.elementAt(index)
        Yaml.default.decodeFromString(serializer(param.type), value)
    }.toTypedArray()

    private val Constructor<*>.goalParameters get() = parameters.drop(1)

    fun toPaperPathfinder(mob: Mob): PaperGoal<Mob> =
        PaperGoal<Mob>(constructor.newInstance(mob.toNMS(), *decoded) as Goal)

    object Serializer : InnerSerializer<Map<String, String>, PathfinderWrapper>(
        serialName = "geary:pathfinder",
        inner = MapSerializer(String.serializer(), String.serializer()),
        transform = {
            PathfinderWrapper(
                type = it["type"] ?: error("Missing type"),
                parameters = it.filterKeys { key -> key != "type" && key != "priority" },
                priority = it["priority"]?.toInt() ?: error("Missing priority")
            )
        },
        inverseTransform = { mapOf("type" to it.type, "priority" to it.priority.toString()) + it.parameters }
    )

    companion object {
        const val PATHFINDER_PACKAGE = "net.minecraft.world.entity.ai.goal."
    }
}
