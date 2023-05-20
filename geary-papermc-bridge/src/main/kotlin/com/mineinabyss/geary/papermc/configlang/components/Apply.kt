package com.mineinabyss.geary.papermc.commons.events.configurable.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:apply_relation")
sealed class Apply

@JvmInline
@Serializable
@SerialName("geary:apply")
value class ApplyBuilder(val entityExpression: String)



