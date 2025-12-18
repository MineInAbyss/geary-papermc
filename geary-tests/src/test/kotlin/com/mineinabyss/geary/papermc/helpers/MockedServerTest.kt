package com.mineinabyss.geary.papermc.helpers

import co.touchlab.kermit.Logger
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearySetup
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.geary.papermc.WorldManager
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.FeatureManager
import com.mineinabyss.idofront.features.FeatureManagerBuilder
import com.mineinabyss.idofront.features.singleFeatureManager
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
import org.koin.dsl.binds
import org.koin.dsl.module
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.inventory.ItemStackMock

abstract class MockedServerTest : GearyTest() {
    init {
        ByteBuddyAgent.install();
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

    val TestMinecraftModule
        get() = TestEngineModule.run {
            copy(module = module {
                single<Plugin> { plugin }
                single<GearyPaperConfig> { GearyPaperConfig() }
                single { ComponentLogger.fallback() } binds arrayOf(ComponentLogger::class, Logger::class)
                single<WorldManager> {
                    WorldManager().apply {
                        setGlobalEngine(get())
                    }
                }
                includes(module)
                with(plugin) {
                    singleFeatureManager {
                        setupFeatureManager()
                    }
                }
            })
        }


    final override fun setupGeary(): Geary {
        val server = MockBukkit.mock()
        mcContext = MinecraftContext(
            server,
            MockBukkit.createMockPlugin("Geary"),
            server.addSimpleWorld("world")
        )
        return geary(TestMinecraftModule) {
            setupGeary()
        }
    }

    init {
        val configModule = object : GearyPaperModule {
            private val koin = world.getKoin()

            override val plugin = koin.get<Plugin>() as JavaPlugin
            override val config: GearyPaperConfig = koin.get()
            override val logger: ComponentLogger = koin.get()
            override val features: FeatureManager = koin.get()
            override val worldManager: WorldManager = koin.get<WorldManager>()
        }
        registerItemService()
        DI.add<GearyPaperModule>(configModule)
        world.getKoin().get<FeatureManager>().load()
        world.getKoin().get<FeatureManager>().enable()
    }

    open fun GearySetup.setupGeary() {}

    open fun FeatureManagerBuilder.setupFeatureManager() {}


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
        MockBukkit.unmock()
        DI.clear()
    }
}
