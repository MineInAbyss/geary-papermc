package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.serialization.MaterialByNameSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material

@Serializable
@SerialName("geary:check.block_type")
class BlockConditions(
    val allow: Set<@Serializable(with = MaterialByNameSerializer::class) Material> = setOf(),
    val deny: Set<@Serializable(with = MaterialByNameSerializer::class) Material> = setOf(),
    val at: Input<@Contextual Location> = Input.reference("location"),
)

fun GearyModule.createBlockConditionChecker() = listener(
    object : ListenerQuery() {
        val conditions by source.get<BlockConditions>()
    }
).check {
    val location = conditions.at.get(this)
    location.block.type.let {
        (conditions.allow.isEmpty() || it in conditions.allow) && it !in conditions.deny
    }
}
