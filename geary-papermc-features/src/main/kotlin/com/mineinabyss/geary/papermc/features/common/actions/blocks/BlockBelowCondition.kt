package com.mineinabyss.geary.papermc.features.common.actions.blocks

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.idofront.location.down
import com.mineinabyss.idofront.serialization.MaterialByNameSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
@SerialName("geary:block_below")
class BlockBelowCondition(
    val allow: Set<@Serializable(with = MaterialByNameSerializer::class) Material>,
    val deny: Set<@Serializable(with = MaterialByNameSerializer::class) Material>,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val blockBelow = location.down(1).block.type
        return (allow.isEmpty() || blockBelow in allow) && blockBelow !in deny
    }
}
