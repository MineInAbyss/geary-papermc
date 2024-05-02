package com.mineinabyss.geary.papermc.modelengine.animation

import com.mineinabyss.geary.papermc.modelengine.toModelEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.ticxo.modelengine.api.animation.ModelState
import com.ticxo.modelengine.api.animation.handler.AnimationHandler

val BukkitEntity.isModelEngineEntity: Boolean
    get() = toModelEntity() != null

fun BukkitEntity.playAnimation(stateName: String, lerpIn: Double, lerpOut: Double, speed: Double, force: Boolean) {
    toModelEntity()?.models?.values?.forEach {
        val state = ModelState.get(stateName) ?: return
        val defaultProperty = AnimationHandler.DefaultProperty(state, stateName, lerpIn, lerpOut, speed)
        it.animationHandler.setDefaultProperty(defaultProperty)
    }
}

fun BukkitEntity.stopAnimation(state: String, ignoreLerp: Boolean = true) {
    toModelEntity()?.models?.values
        ?.forEach { it.animationHandler.getDefaultProperty(ModelState.get(state)) }
}
