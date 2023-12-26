package com.mineinabyss.geary.papermc.configlang.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:applyRelation")
sealed class Apply

@JvmInline
@Serializable
@SerialName("geary:apply")
value class ApplyBuilder(val entityExpression: String)



