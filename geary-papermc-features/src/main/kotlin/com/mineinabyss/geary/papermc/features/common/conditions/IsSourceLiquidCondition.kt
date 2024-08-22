package com.mineinabyss.geary.papermc.features.common.conditions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.block.data.Levelled

@Serializable
@SerialName("geary:is_source_liquid")
class IsSourceLiquidCondition : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val levelled = location?.block?.blockData as? Levelled ?: return false
        return levelled.level == 0
    }
}
