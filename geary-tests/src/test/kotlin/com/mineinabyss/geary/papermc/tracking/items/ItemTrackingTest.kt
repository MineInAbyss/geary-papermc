package com.mineinabyss.geary.papermc.tracking.items


//class ItemTrackingTest : MockedServerTest() {
//    init {
//        geary(TestEngineModule) {
//            withMockTracking()
//        }
//    }
//
//    val prefabKey = PrefabKey.of("test:prefab")
//    val prefab = entity {
//        set(SetItem(SerializableItemStack(type = Material.GLASS)))
//        setPersisting(SomeData("test"))
//        set(prefabKey)
//    }
//
//    @Nested
//    inner class ItemProviderTests {
//        @Test
//        fun `should only encode prefab to PDC when creating item`() {
//            // act
//            val item = gearyItems.createItem(prefabKey)
//
//            // assert
//            item.shouldNotBeNull()
//            item.type.shouldBe(Material.GLASS)
//
//            val pdc = item.itemMeta.persistentDataContainer
//            pdc.decodePrefabs().shouldContainExactly(prefabKey)
//            pdc.decode<SomeData>().shouldBeNull()
//        }
//    }
//
//    @Test
//    fun `should create item cache when player logs in`() {
//        // arrange
//        val player = server.addPlayer()
//
//        // act
//        val cache = player.toGeary().get<PlayerItemCache<*>>()
//        val gearyInv = player.inventory.toGeary()
//
//        // assert
//        cache.shouldNotBeNull()
//        gearyInv.shouldNotBeNull()
//    }
//
//    @Test
//    fun `should get up-to-date entity with ItemStack component when item added to inventory`() {
//        // arrange
//        val player = server.addPlayer()
//        val item = gearyItems.createItem(prefabKey).shouldNotBeNull()
//        val inventory = player.inventory
//        val gearyInv = inventory.toGeary().shouldNotBeNull()
//
//        // act
//        inventory.setItem(10, item)
//        val entityInInv = gearyInv.get(10)
//
//        // assert
//        entityInInv.shouldNotBeNull()
//        entityInInv.get<ItemStack>().shouldBe(item)
//    }
//}
