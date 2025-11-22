package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.papermc.features.entities.sounds.OverrideMobSoundsListener.Companion.makeSound
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlin.random.Random

fun WorldScoped.playAmbientSounds() = system(query<BukkitEntity, Sounds>())
    .every(1.ticks)
    .exec { (bukkit, sounds) ->
        if (Random.nextDouble() < sounds.ambientChance)
            makeSound(bukkit, sounds.ambient)
    }
