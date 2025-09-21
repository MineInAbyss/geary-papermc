# Mobs

!!! note
    To avoid confusion, Geary comes with a `BukkitEntity` typealias for Bukkit's `Entity` interface.

All bukkit entities get a geary entity created for them when spawned (and removed when despawned.)

## Entity syntax

```kotlin
val pig: Pig

// Get the associated geary entity
val gearyPig = pig.toGeary()
pig.set(SomeData())

// Get the bukkit entity using components
pig.get<BukkitEntity>()
pig.toBukkit() // equivalent

// each entity gets both the BukkitEntity component, and the specific instance
pig.get<Pig>()
pig.toBukkit<Pig>() // attempts to cast get<BukkitEntity>()
```

## Persisting data

Any persisting components will be saved and loaded to the mob's persistent data container on NBT save/load. 

```kotlin
@Serializable
@SerialName("myplugin:owner")
class Owner(
    val uuid: @SerializableWith(UUIDSerializer::class) UUID
)

val pig: Pig
val player: Player

pig.setPersisting(Owner(player.uniqueId))
```
