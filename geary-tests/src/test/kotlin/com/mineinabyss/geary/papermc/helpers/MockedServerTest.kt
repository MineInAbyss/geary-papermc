package com.mineinabyss.geary.papermc.helpers

import be.seeseemelk.mockbukkit.MockBukkit
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.GearyPaperConfigModule
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.di.DI
import org.junit.jupiter.api.AfterAll

abstract class MockedServerTest {
    val server = MockBukkit.mock()
    val plugin = MockBukkit.createMockPlugin("Geary")
    val world = server.addSimpleWorld("world")

    init {
        DI.add<GearyPaperConfigModule>(object : GearyPaperConfigModule {
            override val plugin = this@MockedServerTest.plugin
            override val configHolder: IdofrontConfig<GearyPaperConfig>
                get() = error("No config holder in tests, use config directly")
            override val config = GearyPaperConfig()
        })
    }

    @AfterAll
    fun clearMocks() {
        MockBukkit.unmock()
        DI.clear()
    }
}
