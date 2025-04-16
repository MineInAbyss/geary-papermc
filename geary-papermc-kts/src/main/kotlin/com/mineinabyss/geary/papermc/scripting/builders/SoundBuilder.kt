package com.mineinabyss.geary.papermc.scripting.builders

import org.bukkit.Location

class SoundBuilder(
    val key: String
) {
}

fun playSound(key: String, at: Location) {

    println("Playing $key")
}
