package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material

@Serializable
@SerialName("geary:check.block_type")
class BlockConditions(
    val allow: Set<Material> = setOf(),
    val deny: Set<Material> = setOf(),
    val at: Input<@Contextual Location> = Input.reference("event.location"),
)

class BlockConditionChecker : CheckingListener() {
    private val Pointers.conditions by get<BlockConditions>().on(source)

    override fun Pointers.check(): Boolean {
        val location = conditions.at.get(this)
        return location.block.type.let {
            (conditions.allow.isEmpty() || it in conditions.allow) && it !in conditions.deny
        }
    }
}
