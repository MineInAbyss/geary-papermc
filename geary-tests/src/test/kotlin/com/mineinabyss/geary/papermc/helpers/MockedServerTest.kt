package com.mineinabyss.geary.papermc.helpers

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.WorldManager
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.idofront.services.ItemProvider
import com.mineinabyss.idofront.services.SerializableItemStackService
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import org.bukkit.World
import org.bukkit.entity.Pig
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterAll
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.inventory.ItemStackMock

abstract class MockedServerTest : GearyTest() {
    init {
        ByteBuddyAgent.install()
        ByteBuddy().redefine(ItemStackMock::class.java)
            .method(ElementMatchers.named("editPersistentDataContainer"))
            .intercept(MethodDelegation.to(ItemStackMockInterceptor::class.java))
            .make()
            .load(this::class.java.classLoader, ClassReloadingStrategy.fromInstalledAgent())
    }

    data class MinecraftContext(val server: ServerMock, val plugin: JavaPlugin, val world: World)

    private lateinit var mcContext: MinecraftContext
    val server get() = mcContext.server
    val plugin get() = mcContext.plugin
    val mcWorld get() = mcContext.world

    fun createTestPlayer() = server.addPlayer().also {
        EntityAddToWorldEvent(it, mcWorld).callEvent()// TODO MockBukkit does not fire this event
    }

    fun spawnTestEntity() = mcWorld.spawn(mcWorld.spawnLocation, Pig::class.java).also {
        EntityAddToWorldEvent(it, mcWorld).callEvent()
    }

    final override fun setupGeary(): Geary {
        return geary(TestEngineModule).also { it.setupGeary() }
    }

    init {
        println("Initializing bukkit mock")
        val server = MockBukkit.mock()
        mcContext = MinecraftContext(
            server,
            MockBukkit.createMockPlugin("Geary"),
            server.addSimpleWorld("world")
        )
        val configModule = object : GearyPlugin, Plugin by MockBukkit.createMockPlugin() {
            val di = scope {
                single<Plugin> { this@MockedServerTest.plugin }
                single<GearyPaperConfig> { GearyPaperConfig() }
                single { ComponentLogger.fallback() }
                single<WorldManager> {
                    WorldManager().apply {
                        setGlobalEngine(this@MockedServerTest.world)
                    }
                }
            }
            override val config: GearyPaperConfig = di.get()
            override val logger: ComponentLogger = di.get()
            override val features: DIScope = di.get()
            override fun configure(builder: WorldScoped.() -> Unit): AutoCloseable {
                return world.newScope().apply(builder)
            }

            override fun forEachWorld(builder: Geary.() -> Unit) {
                world.apply(builder)
            }

            override val worldManager: WorldManager = di.get()
        }
        GearyPlugin.instance = configModule
        registerItemService()
        configModule.di.setupPaper()
    }

    open fun Geary.setupGeary() {}
    open fun DI.Scope.setupPaper() {}

    private fun registerItemService() {
        Services.register<SerializableItemStackService>(plugin, object : SerializableItemStackService {
            var provider: ItemProvider? = null
            override fun registerProvider(prefix: String, provider: ItemProvider) {
                this.provider = provider
            }

            override fun getProvider(prefix: String): ItemProvider {
                return provider!!
            }
        })
    }

    @AfterAll
    fun clearMocks() {
        println("Unmocking bukkit")
        MockBukkit.unmock()
        GearyPlugin.instance = null
    }
}
