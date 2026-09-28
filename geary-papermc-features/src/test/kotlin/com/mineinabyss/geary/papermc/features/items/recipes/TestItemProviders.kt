package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.idofront.services.ItemProvider
import com.mineinabyss.idofront.services.SerializableItemStackService

object TestItemProviders : SerializableItemStackService {
    private val providers = mutableMapOf<String, ItemProvider>()

    override fun registerProvider(prefix: String, provider: ItemProvider) {
        providers[prefix] = provider
    }

    override fun getProvider(prefix: String): ItemProvider? = providers[prefix]
}
