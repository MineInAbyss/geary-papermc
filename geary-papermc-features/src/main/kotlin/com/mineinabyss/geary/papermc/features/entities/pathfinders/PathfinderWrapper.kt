package com.mineinabyss.geary.papermc.features.entities.pathfinders

import com.charleskorn.kaml.Yaml
import com.destroystokyo.paper.entity.ai.PaperVanillaGoal
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

@Serializable(with = PathfinderWrapper.Serializer::class)
data class PathfinderWrapper(
    val type: String,
    val parameters: Map<String, String>,
    val priority: Int,
) {
    @Transient
    val constructor: Constructor<*> = Class.forName(PATHFINDER_PACKAGE + type)
        .constructors.filter {
            it.parameterCount == (parameters.size + 1) && NMSMob::class.java.isAssignableFrom(it.parameters.first().type)
        }.single {
            it.parameters.drop(1).map { it.name }.containsAll(parameters.keys)
        }

    @Transient
    val serializers = constructor.parameters.drop(1).map { it.name to serializer(it.type) }

    @Transient
    val decoded = serializers.map { (name, serializer) ->
        Yaml.default.decodeFromString(serializer, parameters[name] ?: error("Missing parameter $name"))
    }.toTypedArray()

    fun toPaperPathfinder(mob: Mob): PaperVanillaGoal<Mob> =
        PaperVanillaGoal<Mob>(constructor.newInstance(mob.toNMS(), *decoded) as Goal)

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
