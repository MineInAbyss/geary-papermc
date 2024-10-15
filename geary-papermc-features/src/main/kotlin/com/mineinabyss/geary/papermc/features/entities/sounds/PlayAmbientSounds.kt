package com.mineinabyss.geary.papermc.features.entities.sounds

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.features.entities.sounds.OverrideMobSoundsListener.Companion.makeSound
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlin.random.Random

fun Geary.playAmbientSounds() = system(object : Query() {
    val bukkit by get<BukkitEntity>()
    val sounds by get<Sounds>()
}).every(1.ticks).exec {
    if (Random.nextDouble() < sounds.ambientChance)
        makeSound(bukkit, sounds.ambient)
}
