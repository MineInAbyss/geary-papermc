package com.mineinabyss.geary.papermc.creativetabs

import com.mineinabyss.geary.papermc.ExternalItemLoading
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.messaging.ComponentLogger
import io.netty.buffer.Unpooled
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.SharedConstants
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.scheduler.BukkitTask
import java.util.*
import net.minecraft.world.item.ItemStack as NMSItemStack

/**
 * Talks the MineInAbyss Addon mod's creative tab protocol.
 * A client says hello with the addon protocol and its game protocol, and from then on gets a clear followed by every section whenever they change.
 * Sections only go to players holding [PERMISSION], which is checked every second so granting or revoking it applies without a rejoin.
 * The client decides how a section's entries are laid out into tabs.
 * Items go over the wire in the game's own codec, which only lines up when both sides run the same game version
 */
class CreativeTabsChannel(private val plugin: Plugin, private val logger: ComponentLogger) : PluginMessageListener {
    /** Clients that said hello, mapped to whether they held [PERMISSION] when last sent to */
    private val clients = mutableMapOf<UUID, Boolean>()
    private var packets = emptyList<ByteArray>()
    private var isOpen = false
    private var rebuildQueued = false
    private var permissionCheck: BukkitTask? = null

    fun open() {
        isOpen = true
        permissionCheck = Bukkit.getScheduler().runTaskTimer(plugin, Runnable { resendOnPermissionChange() }, 20, 20)
        Bukkit.getMessenger().apply {
            registerIncomingPluginChannel(plugin, HELLO, this@CreativeTabsChannel)
            registerOutgoingPluginChannel(plugin, CLEAR)
            registerOutgoingPluginChannel(plugin, SECTION)
        }
        // A reload of this feature starts empty while clients only say hello on join, the section channel only exists in clients speaking this protocol
        Bukkit.getOnlinePlayers().filter { SECTION in it.listeningPluginChannels }.forEach { clients[it.uniqueId] = false }
    }

    fun close() {
        isOpen = false
        permissionCheck?.cancel()
        clients.clear()
        Bukkit.getMessenger().apply {
            unregisterIncomingPluginChannel(plugin, HELLO, this@CreativeTabsChannel)
            unregisterOutgoingPluginChannel(plugin, CLEAR)
            unregisterOutgoingPluginChannel(plugin, SECTION)
        }
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != HELLO) return
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(message))
        val (protocol, gameProtocol) = runCatching { buf.readVarInt() to buf.readVarInt() }.getOrElse {
            return logger.w { "Malformed creative tabs hello from ${player.name}" }
        }
        if (protocol != PROTOCOL || gameProtocol != SharedConstants.getProtocolVersion()) {
            return logger.i { "Not sending creative tabs to ${player.name}, addon protocol $protocol and game protocol $gameProtocol do not match $PROTOCOL and ${SharedConstants.getProtocolVersion()}" }
        }
        send(player)
    }

    private fun send(player: Player) {
        val permitted = player.hasPermission(PERMISSION)
        clients[player.uniqueId] = permitted
        player.sendPluginMessage(plugin, CLEAR, ByteArray(0))
        if (permitted) packets.forEach { player.sendPluginMessage(plugin, SECTION, it) }
    }

    private fun resendOnPermissionChange() {
        clients.toList().forEach { (uuid, permitted) ->
            val player = Bukkit.getPlayer(uuid) ?: return@forEach
            if (player.hasPermission(PERMISSION) != permitted) send(player)
        }
    }

    fun forget(player: Player) {
        clients -= player.uniqueId
    }

    /**
     * Items for Nexo backed prefabs only exist once Nexo has registered them, after prefabs are read.
     * The loading flags are only checked a tick later, since this feature can load before the prefabs feature flags its own load in the same tick.
     * Requests made while one is already waiting share its rebuild, startup asks twice at once
     */
    fun rebuildOnceLoaded() {
        if (rebuildQueued) return
        rebuildQueued = true
        Bukkit.getScheduler().runTask(plugin, Runnable {
            PrefabLoading.awaitLoaded()
                .thenCompose { ExternalItemLoading.awaitLoaded() }
                .thenRun {
                    Bukkit.getScheduler().runTask(plugin, Runnable {
                        rebuildQueued = false
                        if (isOpen) rebuild()
                    })
                }
        })
    }

    private fun rebuild() {
        val sections = buildCreativeSections(gearyPaper.worldManager.global)
        packets = sections.flatMap(::encode)
        logger.i { "Built creative tabs ${sections.joinToString { "${it.id} (${it.entries.size})" }}" }
        clients.keys.mapNotNull(Bukkit::getPlayer).forEach(::send)
    }

    private fun encode(section: CreativeSection): List<ByteArray> {
        val title = PaperAdventure.asVanilla(section.title)
        val icon = CraftItemStack.asNMSCopy(section.entries.first().item)
        return section.entries.chunked(ENTRIES_PER_PACKET).map { chunk ->
            val buf = RegistryFriendlyByteBuf(Unpooled.buffer(), MinecraftServer.getServer().registryAccess())
            try {
                ByteBufCodecs.STRING_UTF8.encode(buf, section.id)
                ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, title)
                NMSItemStack.OPTIONAL_STREAM_CODEC.encode(buf, icon)
                buf.writeVarInt(chunk.size)
                chunk.forEach { entry ->
                    NMSItemStack.OPTIONAL_STREAM_CODEC.encode(buf, CraftItemStack.asNMSCopy(entry.item))
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.key.full)
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.category)
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.vanillaTab)
                }
                ByteArray(buf.readableBytes()).also(buf::readBytes)
            } finally {
                buf.release()
            }
        }
    }

    companion object {
        const val PROTOCOL = 2
        const val PERMISSION = "geary.creativetabs"
        const val HELLO = "mineinabyss:creative_tabs/hello"
        const val CLEAR = "mineinabyss:creative_tabs/clear"
        const val SECTION = "mineinabyss:creative_tabs/section"

        // Keeps a packet well under the 1 MiB plugin message limit, with headroom for items carrying long lore or large custom data
        private const val ENTRIES_PER_PACKET = 128
    }
}
