package com.mineinabyss.geary.papermc.features.common.conditions.location

import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
data class BlockConditions(
    val allow: MaterialOrTagMatcher? = null,
    val deny: MaterialOrTagMatcher? = null,
    val isSolid: Boolean? = null,
) {
    fun check(location: Location?): CheckResult {
        val material = location?.block?.type ?: return CheckResult.Success
        return checks {
            checkOptional("Allowed materials", allow) { it.matches(material) }
            checkOptional("Denied materials", deny) { it.notMatches(material) }
            checkOptional("Solid", isSolid) { it == material.isSolid }
        }
    }
}
