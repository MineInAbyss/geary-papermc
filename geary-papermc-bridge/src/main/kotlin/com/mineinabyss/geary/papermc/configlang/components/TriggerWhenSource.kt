package com.mineinabyss.geary.papermc.configlang.components

import com.mineinabyss.geary.datatypes.GearyEntityType

data class TriggerWhenSource(
    val runEvents: GearyEntityType,
    val runAsSource: Boolean,
)

data class TriggerWhenTarget(
    val runEvents: GearyEntityType,
    val runAsSource: Boolean,
)
