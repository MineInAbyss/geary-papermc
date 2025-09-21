Geary uses prefabs to define custom mob, item, or block types. These are just entities with a unique key, which we use to persist information ingame. For instance, items and mobs store a prefab key in their persistent data container, and blocks let you directly access a prefab based on their blockdata.

Geary will automatically load prefabs under `plugins/Geary/<namespace>/...`. However, you may want your configs to be kept in your own plugin folder instead, or package them directly with your jar file. For this, use the `Prefabs` addon as shown below.

## Load prefabs from code

You may create prefabs from arbitrary entities, the prefabs addon provides some helper functions for this:

```kotlin
namespace("my_namespace") {
    prefabs {
        create(
            // creates prefab with key my_namespace:name
            "name" to geary.entity {
                set(MyCustomData())
            },
            ...
        )
    }
}
```

## Load prefabs from your jar resources

Plugins can load prefabs from their jar's resources

```kotlin
namespace("my_namespace") {
    prefabs {
        // We pass a reference to our plugin class to use the correct classLoader
        fromJarResources(MyPlugin::class, "prefab1.yml", "folder/prefab2.yml")

        // You can also load all prefabs inside a folder, including subfolders
        fromJarResourceDirectory(MyPlugin::class, "prefabs")
    }
}
```

## Load prefabs from files

```kotlin
namespace("my_namespace") {
    prefabs {
        // Load specific prefab files
        fromFiles(dataFolder.toPath() / "my-prefab.yml", ...)
        
        // Load all prefabs in a folder on the filesystem
        fromDirectory(dataFolder.toPath() / "prefabs")
    }
}
```
