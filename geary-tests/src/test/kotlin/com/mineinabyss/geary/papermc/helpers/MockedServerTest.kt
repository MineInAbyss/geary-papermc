package com.mineinabyss.geary.papermc.helpers

import be.seeseemelk.mockbukkit.MockBukkit
import com.mineinabyss.geary.papermc.*
import com.mineinabyss.geary.test.GearyTest
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.junit.jupiter.api.AfterAll

abstract class MockedServerTest : GearyTest() {
    init {
        MockBukkit.unmock()
    }

    val server = MockBukkit.mock()
    val plugin = MockBukkit.createMockPlugin("Geary")
    val world = server.addSimpleWorld("world")

    init {
        DI.add<GearyPaperModule>(object : GearyPaperModule {
            override val plugin: GearyPlugin = TODO()
            override val configHolder: IdofrontConfig<GearyPaperConfig>
                get() = error("No config holder in tests, use config directly")
            override val config = GearyPaperConfig()

            override val logger: ComponentLogger = ComponentLogger.fallback()
            override val features: Features = Features(this@MockedServerTest.plugin)
            override val gearyModule get() = error("Should not access module here in tests")
            override val worldManager = WorldManager()
        })
    }

    @AfterAll
    fun clearMocks() {
        MockBukkit.unmock()
        DI.clear()
    }
}
