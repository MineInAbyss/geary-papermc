package com.mineinabyss.geary.papermc.features.items.lightsource

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.math.floor

/**
 * Sent to everyone nearby rather than only the owner, so players exploring together see the same lit area.
 * Only air is ever replaced, so the light does not appear underwater or in a tight gap
 */
object PlayerLightBlocks {
    /** Viewers are kept so anyone who walks away can be sent the real block back */
    private data class Placed(
        val world: UUID,
        val x: Int,
        val y: Int,
        val z: Int,
        val level: Int,
        val viewers: Set<UUID>,
    ) {
        fun isAt(block: Block) = world == block.world.uid && x == block.x && y == block.y && z == block.z

        fun isIn(chunk: Chunk) = world == chunk.world.uid && x shr 4 == chunk.x && z shr 4 == chunk.z
    }

    private data class Want(val level: Int, val forwardOffset: Double)

    private val placed = mutableMapOf<UUID, Placed>()
    private val wanted = mutableMapOf<UUID, Want>()

    private val lightByLevel = Array(16) { level -> Material.LIGHT.createBlockData { (it as Levelled).level = level } }

    /** Fine enough that a one block gap in the ray is never stepped over */
    private const val STEP = 0.5

    /** Comfortably past the light's own reach so it never pops in for others */
    private const val VIEW_RANGE_SQUARED = 32.0 * 32.0

    /** Null removes the player's light */
    fun update(player: Player, config: LightSource?) {
        config ?: return release(player)
        wanted[player.uniqueId] = Want(config.level, config.forwardOffset)
        refresh(player, player.location, recomputeViewers = true)
    }

    fun hasLight(player: Player) = player.uniqueId in wanted

    fun tracksOrientation(player: Player) = (wanted[player.uniqueId]?.forwardOffset ?: 0.0) > 0.0

    /**
     * [at] differs from [Player.getLocation] during a move event.
     * Viewers are only rebuilt when asked, so a moving player does not rebuild them up to twenty times a second
     */
    fun refresh(player: Player, at: Location, recomputeViewers: Boolean) {
        val want = wanted[player.uniqueId] ?: return
        val target = target(player, at, want.forwardOffset) ?: return clear(player)
        val current = placed[player.uniqueId]

        val oldViewers = current?.viewers.orEmpty()
        val viewers = if (recomputeViewers || current == null) viewersOf(player, at) else oldViewers
        val samePlace = current?.isAt(target) == true
        val unchanged = samePlace && current.level == want.level
        if (unchanged && oldViewers == viewers) return

        val data = lightByLevel[want.level]
        val location = target.location
        for (id in viewers) {
            if (unchanged && id in oldViewers) continue
            Bukkit.getPlayer(id)?.sendBlockChange(location, data)
        }

        // Reverting after the new block is sent avoids a frame without light.
        // A viewer just sent light at this same block would have it wiped again
        current?.let { old ->
            for (id in old.viewers) {
                if (samePlace && id in viewers) continue
                Bukkit.getPlayer(id)?.let { revertFor(it, old) }
            }
        }

        placed[player.uniqueId] = Placed(target.world.uid, target.x, target.y, target.z, want.level, viewers)
    }

    /** The client drops a block change for a chunk it has not received yet, so lights are sent again once it has */
    fun resend(viewer: Player, chunk: Chunk) {
        for (light in placed.values) {
            if (viewer.uniqueId !in light.viewers || !light.isIn(chunk)) continue
            val location = Location(chunk.world, light.x.toDouble(), light.y.toDouble(), light.z.toDouble())
            viewer.sendBlockChange(location, lightByLevel[light.level])
        }
    }

    fun clear(player: Player) {
        val current = placed.remove(player.uniqueId) ?: return
        current.viewers.forEach { id -> Bukkit.getPlayer(id)?.let { revertFor(it, current) } }
    }

    fun release(player: Player) {
        wanted.remove(player.uniqueId)
        clear(player)
    }

    private fun viewersOf(player: Player, at: Location): Set<UUID> = buildSet {
        // A player is never in their own tracker
        add(player.uniqueId)
        player.trackedBy
            .filter { it.world == at.world && it.location.distanceSquared(at) <= VIEW_RANGE_SQUARED }
            .mapTo(this) { it.uniqueId }
    }

    private fun target(player: Player, at: Location, forwardOffset: Double): Block? {
        val world = at.world
        val eyeX = at.x
        val eyeY = at.y + player.eyeHeight
        val eyeZ = at.z
        if (!world.isChunkLoaded(floor(eyeX).toInt() shr 4, floor(eyeZ).toInt() shr 4)) return null

        val atEye = world.airAt(floor(eyeX).toInt(), floor(eyeY).toInt(), floor(eyeZ).toInt())
            // Crouching under a slab puts the eye inside a block, the feet are usually still in air
            ?: return world.airAt(at.blockX, at.blockY, at.blockZ)
        if (forwardOffset <= 0.0) return atEye

        // Stops at the first solid block so the light never ends up on the far side of a wall
        var furthest = atEye
        val direction = at.direction
        var distance = STEP
        while (distance <= forwardOffset) {
            val x = floor(eyeX + direction.x * distance).toInt()
            val y = floor(eyeY + direction.y * distance).toInt()
            val z = floor(eyeZ + direction.z * distance).toInt()
            distance += STEP
            if (x == furthest.x && y == furthest.y && z == furthest.z) continue
            if (!world.isChunkLoaded(x shr 4, z shr 4)) break
            furthest = world.airAt(x, y, z) ?: break
        }
        return furthest
    }

    // Outside the build height every block reads as air, so the height has to be checked first
    private fun World.airAt(x: Int, y: Int, z: Int): Block? =
        if (y in minHeight until maxHeight) getBlockAt(x, y, z).takeIf { it.type.isAir } else null

    private fun revertFor(viewer: Player, at: Placed) {
        // A viewer who has since changed world would have these coordinates painted onto the wrong place
        if (viewer.world.uid != at.world || !viewer.world.isChunkLoaded(at.x shr 4, at.z shr 4)) return
        val block = viewer.world.getBlockAt(at.x, at.y, at.z)
        viewer.sendBlockChange(block.location, block.blockData)
    }
}
