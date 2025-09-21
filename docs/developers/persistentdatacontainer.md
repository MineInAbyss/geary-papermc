# Persistent data

The module `geary-papermc-datastore` (included in `geary-papermc`) provides functions for encoding and decoding serializable classes (using [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)) for any persistent data container.

This may can be useful for types that aren't supported in geary, but which do have a PDC (ex chunks), or for users that just want to read/write data, and not use a full ECS.

## Syntax

Persisting components must be marked `@Serializable` and have a serial name in the form of `@SerialName("namespace:key")`. The SerialName will be used as a key in the PDC.

```kotlin
@Serializable
@SerialName("myplugin:money")
class Money(amount: Int)

val pdc: PersistentDataContainer

pdc.encode(Money(100))
pdc.decode<Money>()
```
