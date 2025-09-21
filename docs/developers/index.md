# Setup

Add geary-papermc to your gradle project:

```kotlin
repositories {
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    implementation("com.mineinabyss:geary-papermc:$gearyVersion")
}
```

Depend on `geary-papermc` to get all supported addons automatically, or `geary-papermc-core` for just the API.
{.info}


## Initialize your plugin
The Geary-papermc plugin initializes Geary with some useful addons like prefabs and serialization. Other plugins can further configure Geary with their own addons on server startup. Learn more about creating configurable addons [here](../../geary/guide/addons.md)

Create a Geary addon anywhere in your code, we recommend a top level variable:

```kotlin
val MyCustomAddon = createAddon<Unit>("My plugin", configuration = {
    // Install and configure any other addons you like here
    autoscan(classLoader, "my.plugin.package") {
        // Register all serializable classes for use in prefabs/persisting data
        components()
    }
    install(SomeOtherAddon)
}) {
    // Use this block for logic that should run after everything is configured,
    // this includes registering system, creating any custom entities, or startup logic
    
    systems {
        yourSystemHere()
    }
    
    onStart {
        // Your startup logic
    }
}
```

Install your addon in your plugin's `onLoad()` function like so:

```kotlin
override fun onLoad() {
    gearyPaper.configure {
        install(MyCustomAddon)
    }
}
```
