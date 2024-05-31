package com.mineinabyss.geary.papermc.tracking.items

//class SetItemMigrationTest : MockedServerTest() {
//    init {
//        geary(TestEngineModule) {
//            serialization {
//                components {
//                    component(SetItemIgnoredProperties::class, SetItemIgnoredProperties.serializer())
//                }
//            }
//            withMockTracking()
//        }
//    }
//
//    fun itemPrefabs() = sequence<PrefabKey> {
//        val prefabKey1 = PrefabKey.of("test:regular")
//        entity {
//            set(SetItem(SerializableItemStack(type = Material.GLASS)))
//            set(prefabKey1)
//        }
//        yield(prefabKey1)
//    }.asStream()
//
//    @ParameterizedTest
//    @MethodSource("itemPrefabs")
//    fun `should respect SetItem migration when tracking an item created before the migration`(
//        prefab: PrefabKey
//    ) {
//        // arrange
//        val player = server.addPlayer()
//        val inventory = player.inventory
//        val gearyInventory = inventory.toGeary().shouldNotBeNull()
//        val item = gearyItems.createItem(prefab)
//
//        // act
//        val typeBefore = item?.type
//        prefab.toEntity().set(SetItem(SerializableItemStack(type = Material.STONE)))
//        inventory.setItem(1, item)
//        gearyInventory.forceRefresh()
//        val typeAfter = inventory.getItem(1)?.type
//
//        // assert
//        typeBefore.shouldBe(Material.GLASS)
//        typeAfter.shouldBe(Material.STONE)
//    }
//
//    @ParameterizedTest
//    @MethodSource("itemPrefabs")
//    fun `should not migrate item properties when marked as overridden`(
//        prefab: PrefabKey
//    ) {
//        // arrange
//        val player = server.addPlayer()
//        val inventory = player.inventory
//        val gearyInventory = inventory.toGeary().shouldNotBeNull()
//        val item = gearyItems.createItem(prefab).shouldNotBeNull()
//
//        // act
//
//        // Imitate player renaming item
//        item.editMeta {
//            it.displayName(Component.text("Custom Name"))
//            it.persistentDataContainer.encode(SetItemIgnoredProperties(setOf(BaseSerializableItemStack.Properties.DISPLAY_NAME)))
//        }
//
//        prefab.toEntity().set(SetItem(SerializableItemStack(displayName = Component.text("Overridden"))))
//
//        inventory.setItem(1, item)
//        gearyInventory.forceRefresh()
//        val nameAfter = inventory.getItem(1)?.itemMeta?.displayName()
//
//        // assert
//        nameAfter.shouldBe(Component.text("Custom Name"))
//    }
//}
