@file:UseSerializers(ColorSerializer::class, IntRangeSerializer::class, DoubleRangeSerializer::class)

package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.expr
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.SoundCategory

@Serializable
@SerialName("geary:play_sound")
class SoundAction(
    val sound: Expression<String>,
    val category: Expression<SoundCategory> = expr(SoundCategory.MASTER),
    val volume: Expression<Float> = expr(1.0f),
    val pitch: Expression<Float> = expr(1.0f),
) : Action {
    override fun ActionGroupContext.execute() {
        val bukkit = entity.get<BukkitEntity>() ?: return
        bukkit.world.playSound(
            Sound.sound()
                .type(Key.key(eval(sound)))
                .pitch(eval(pitch))
                .volume(eval(volume))
                .source(eval(category))
                .build(),
            bukkit
        )
    }
}
