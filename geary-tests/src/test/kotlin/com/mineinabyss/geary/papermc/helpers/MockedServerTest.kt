package com.mineinabyss.geary.papermc.helpers

import be.seeseemelk.mockbukkit.MockBukkit
import com.mineinabyss.geary.papermc.Features
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.junit.jupiter.api.AfterAll

abstract class MockedServerTest {
    init {
        MockBukkit.unmock()
    }

    val server = MockBukkit.mock()
    val plugin = MockBukkit.createMockPlugin("Geary")
    val world = server.addSimpleWorld("world")

    init {
        DI.add<GearyPaperModule>(object : GearyPaperModule {
            override val plugin = this@MockedServerTest.plugin
            override val configHolder: IdofrontConfig<GearyPaperConfig>
                get() = error("No config holder in tests, use config directly")
            override val config = GearyPaperConfig()

            override val logger: ComponentLogger = ComponentLogger.fallback()
            override val features: Features = Features(this@MockedServerTest.plugin)
        })
    }

    @AfterAll
    fun clearMocks() {
        MockBukkit.unmock()
        DI.clear()
    }
}
