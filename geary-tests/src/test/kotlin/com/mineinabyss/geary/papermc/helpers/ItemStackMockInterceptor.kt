package com.mineinabyss.geary.papermc.helpers

import net.bytebuddy.implementation.bind.annotation.Argument
import net.bytebuddy.implementation.bind.annotation.This
import org.bukkit.persistence.PersistentDataContainer
import org.mockbukkit.mockbukkit.inventory.ItemStackMock
import java.util.function.Consumer

/**
 * Missing implementations on [ItemStackMock] that we manually inject with [net.bytebuddy.ByteBuddy]
 *
 * @see MockedServerTest
 */
object ItemStackMockInterceptor {
    @JvmStatic
    fun editPersistentDataContainer(@This thisRef: ItemStackMock, @Argument(0) arg: Consumer<PersistentDataContainer>): Boolean {
        thisRef.itemMeta = thisRef.itemMeta.apply {
            arg.accept(persistentDataContainer)
        }
        return true
    }
}