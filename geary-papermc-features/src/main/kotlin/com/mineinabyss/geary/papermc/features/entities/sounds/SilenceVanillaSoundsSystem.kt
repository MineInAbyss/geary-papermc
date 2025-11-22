package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun WorldScoped.silenceVanillaSounds() = observe<OnSet>()
    .involving(query<BukkitEntity, Sounds>())
    .exec { (bukkit) ->
        bukkit.isSilent = true
    }
