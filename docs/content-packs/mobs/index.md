# Creating mobs

For mobs to be spawnable by Geary, the prefab must have a component that provides a Minecraft entity.

#### Vanilla entity type

The simplest component, just picks a type registered in Minecraft. This includes any custom mobs that may be registered via NMS.
```yaml
set.entityType: minecraft:slime
```

#### MythicMobs

```yaml
set.mythicMob: <mythicMobKey>
```

Where `<mythicMobKey>` is the root key in your MythicMob mob file, ex:

```yaml
MyCustomPig:
  Type: PIG
  Health: 20
  Options:
    ...
```

## MythicMobs integration

Alternatively, you may create and manage your mobs with MythicMobs and let them inherit any prefab using our prefabs skill:

```yaml
MyCustomPig:
  Skills:
    - prefabs{p=mineinabyss:my_prefab} @self ~onSpawnOrLoad
```
