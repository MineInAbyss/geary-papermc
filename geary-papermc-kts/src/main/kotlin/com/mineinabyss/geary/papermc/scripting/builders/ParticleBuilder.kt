package com.mineinabyss.geary.papermc.scripting.builders

import org.bukkit.Location
import org.bukkit.Particle

class ParticleBuilder {
    private var type: Particle = Particle.BLOCK

    fun type(particle: Particle) {}
    fun location(location: Location) {}
    fun offset(x: Double, y: Double, z: Double) {}
    fun color(color: org.bukkit.Color?) {}
    fun count(count: Int) {}
    fun extra(speed: Double) {}
    fun receivers(radius: Int) {}
    fun data(toItemStackOrNull: Any?) {}
    fun spread(all: Number) {}
    fun spawn() {}
}

fun particle(location: Location, builder: ParticleBuilder.() -> Unit) {
}
