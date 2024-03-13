package com.mineinabyss.geary.papermc.bridge.actions.sounds

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.FloatRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.SoundCategory

@Serializable
@SerialName("geary:play_sound")
class DoSound(
    val sound: String,
    val category: SoundCategory = SoundCategory.MASTER,
    val volume: FloatRange = 1.0f..1.0f,
    val pitch: FloatRange = 1.0f..1.0f,
)

fun GearyModule.createPlaySoundAction() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        val sound by source.get<DoSound>()
    }
).exec {
    with(sound) {
        bukkit.world.playSound(
            Sound.sound()
                .type(Key.key(sound))
                .pitch(pitch.randomOrMin())
                .volume(volume.randomOrMin())
                .source(category)
                .build(),
            bukkit
        )
    }
}
