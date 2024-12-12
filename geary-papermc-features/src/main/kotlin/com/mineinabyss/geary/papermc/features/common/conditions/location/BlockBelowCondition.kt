package com.mineinabyss.geary.papermc.features.common.conditions.location

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.idofront.location.down
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("geary:block_below")
value class BlockBelowCondition(val conditions: BlockConditions) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        return conditions.check(location?.clone()?.down(1)).successOrThrow()
    }
}
