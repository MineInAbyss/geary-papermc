package com.mineinabyss.geary.papermc.api

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackContent
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toBukkit
import com.mineinabyss.geary.papermc.tracking.items.components.Equipped
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class GearyItemDSL(val world: Geary): Geary by world {
    private val item: ItemBuilder = ItemBuilder()

    fun item(block: ItemBuilder.() -> Unit) {
        item.apply(block)
    }

    data class ItemContext(
        val item: GearyEntity,
        /** The player or mob holding this item. */
        val itemHolder: LivingEntity,
        override val coroutineContext: CoroutineContext,
    ): CoroutineScope {
        fun <T> config(string: String): T {
            TODO()
        }

        fun explode(location: Location, power: Number) {
            location.createExplosion(power.toFloat())
        }

        fun particle(location: Location, builder: ParticleDSL.() -> Unit) {
        }


        fun cooldown(text: String, duration: Duration) {
        }

        fun cooldown(duration: Duration) {
        }

        fun playSound(key: String, at: Location) {}

        fun mythicSkills(caster: LivingEntity, vararg skills: String) {

        }
    }

    inline fun <T> on(run: ItemContext.(T) -> Unit) {

    }

//    fun onItemDrop(block: suspend ItemContext.(OnItemDrop) -> Unit) {
//    }
//
//    fun onRightClick(block: suspend ItemContext.(OnItemDrop) -> Unit) {
//    }

    operator fun String.invoke(run: ItemContext.() -> Unit) {

    }

    fun passive(run: PassiveDSL.() -> Unit) {

    }

    inline fun components(vararg component: Any) {

    }
}

fun LivingEntity.nearbyEntities(radius: Int, vararg prefabs: String): Sequence<GearyEntity> {
    TODO()
}

fun LivingEntity.nearestEntity(radius: Int, vararg prefabs: String): GearyEntity? {
    TODO()
}

class PassiveDSL(val entity: GearyEntity) {
    fun match(query: Query, every: Duration, run: suspend GearyItemDSL.ItemContext.() -> Unit) {

    }
}

object Patterns {
    // Runs in a spiral pattern at location, facing dir, with a radius of radius, and a length of length, and *count* points
    inline fun spiral(
        start: Location,
        end: Location,
        radius: Double = 1.0,
        count: Int,
        startOffset: Double = 0.0,
        endOffset: Double = 0.0,
        run: PatternPoint.() -> Unit,
    ) {
    }

    data class PatternPoint(
        val location: Location,
        val direction: Location,
        val distance: Double,
    )
}

class ParticleDSL {
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

class ItemBuilder {
    var type: Material = Material.STONE
    var hideTooltip: Boolean = false

    fun type(key: Material) {
//        type = Material.getMaterial(key) ?: Material.STONE
    }

    fun name(name: String) {
    }

    fun lore(vararg lore: String) {

    }

    fun customModelData(int: Int) {

    }

    fun enchantments(vararg enchantments: Pair<Enchantment, Int>) {
    }

    fun itemFlags(vararg itemFlags: ItemFlag) {

    }
}

fun customItem(block: GearyItemDSL.() -> Unit): GearyItemDSL {
    return GearyItemDSL(gearyPaper.worldManager.global).apply(block)
}

fun <T> customItem(block: GearyItemDSL.(T) -> Unit): GearyItemDSL {
    TODO()
//    return GearyItemDSL().apply(block)
}

fun <T> skill(run: (T) -> Unit) {

}
