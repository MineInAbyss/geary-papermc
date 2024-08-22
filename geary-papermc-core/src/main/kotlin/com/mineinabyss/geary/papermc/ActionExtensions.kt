package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.Location

var ActionGroupContext.location: Location
    get() = (environment["location"] as? Location)
        ?: entity.get<BukkitEntity>()?.location
        ?: error("No location found in context")
    set(value) {
        environment["location"] = value
    }
