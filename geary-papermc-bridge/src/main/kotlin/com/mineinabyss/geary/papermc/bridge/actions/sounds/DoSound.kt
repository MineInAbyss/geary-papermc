package com.mineinabyss.geary.papermc.bridge.actions.sounds

import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.FloatRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.SoundCategory

@Serializable
@SerialName("geary:play_sound")
class DoSound(
    val sound: String,
    val category: SoundCategory = SoundCategory.MASTER,
    val volume: FloatRange = 1.0f..1.0f,
    val pitch: FloatRange = 1.0f..1.0f,
)

@AutoScan
class DoPlaySoundSystem : GearyListener() {
    private val Pointers.bukkit by get<BukkitEntity>().on(target)
    private val Pointers.sound by get<DoSound>().on(source)

    override fun Pointers.handle() {
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
}
