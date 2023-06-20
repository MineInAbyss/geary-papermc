package com.mineinabyss.geary.papermc.configlang.helpers

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.commons.events.configurable.components.EventCondition
import com.mineinabyss.geary.prefabs.helpers.addPrefab

fun GearyEntityType.runFollowUp(runAsSource: Boolean, current: GearyEntity, other: GearyEntity) {
    val withSource = if (runAsSource) current else other
    val withTarget = if (runAsSource) other else current
    forEach {
        val triggerEntity = it.toGeary()
        val conditionEntity = withSource.getRelation<EventCondition>(triggerEntity)?.entity
        if (conditionEntity == null || withTarget.callCheck(source = current) {
                addPrefab(conditionEntity.toGeary())
            }) {
            withTarget.callEvent(triggerEntity, source = withSource)
        }
    }
}

inline fun GearyEntity.callCheck(
    source: GearyEntity? = null,
    crossinline init: GearyEntity.() -> Unit,
): Boolean = callEvent(
    init = {
        init()
        add<RequestCheck>()
    },
    source = source,
    result = { !it.has<FailedCheck>() }
)
