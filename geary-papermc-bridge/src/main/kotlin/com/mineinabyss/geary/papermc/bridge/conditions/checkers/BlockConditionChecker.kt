package com.mineinabyss.geary.papermc.bridge.conditions.checkers

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.BlockConditions
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Location

class BlockConditionChecker : CheckingListener() {
    private val Pointers.conditions by get<BlockConditions>().on(target)

    private val Pointers.location by get<Location>().on(event)

    override fun Pointers.check(): Boolean {
        return location.block.type.let {
            (conditions.allow.isEmpty() || it in conditions.allow) && it !in conditions.deny
        }
    }
}
