package com.mineinabyss.geary.papermc.features.common.conditions.location

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.idofront.serialization.MaterialByNameSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material

@Serializable
@SerialName("geary:block")
class BlockCondition(
    val allow: Set<@Serializable(with = MaterialByNameSerializer::class) Material> = setOf(),
    val deny: Set<@Serializable(with = MaterialByNameSerializer::class) Material> = setOf(),
) : Condition {
    override fun ActionGroupContext.execute(): Boolean = check(location, allow, deny)

    companion object {
        fun check(location: Location?, allow: Set<Material>, deny: Set<Material>): Boolean {
            val blockBelow = location?.block?.type ?: return true
            return (allow.isEmpty() || blockBelow in allow) && blockBelow !in deny
        }
    }
}
