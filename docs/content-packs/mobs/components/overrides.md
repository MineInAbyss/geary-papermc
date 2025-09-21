# Overrides

Components that replace vanilla behaviour with custom ones, often reimplemented by us.

## Any mob

### Death Loot
Items and exp to drop on a mob's death
```yaml
deathLoot:
  exp: 10..20
  deathCommands: [ "say Hi" ] # (1)!
  drops: # (2)!
    - item: { type: "apple" }
      cooked: { type: "golden_apple" } # (4)!
      cookExp: ... # (5)!
      cookTime: ... # (6)!
      amount: 1..2
      dropChance: 0.5
  ignoredCauses: [ SUFFOCATION ] # (3)!
```

1. List of commands to run on death
2. A list of items to drop
3. **[DamageCause](https://jd.papermc.io/paper/1.20/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html)**, Do not drop items when entity dies of this cause
4. Item to drop if using fire aspect on mob
5. Default `0`, Amount of exp for cooking
6. Default `200`. Amount of time cooking takes

### Sounds
Custom sound system, can play idle sounds and when interacting with entity, all optional

```yaml
sounds:
  step: <Sound>
  ambient: <Sound>
  death: <Sound>
  hurt: <Sound>
  splash: <Sound>
  swim: <Sound>
```

#### Sound
```yaml
ambient:
    sound: "minecraft:entity.pig.ambient" # (1)!
    volume: ... # (2)!
    pitch: ... # (3)!
    pitchRange: ... # (4)!
    category: ... # (5)!
```

1. Sound to play, anything in `/playsound` works here
2. Default `1.0`, Volume of the sound
3. Default `1.0`, Pitch of the sonud 
4. Default `0.2`, range pitch can go up or down by randomly
5. Default `MASTER`, Any of [SoundCategory](https://jd.papermc.io/paper/1.20/org/bukkit/SoundCategory.html)

### Display name
Name to display in chat when killed by entity.

```yaml
displayName: "<red>Scary Pig" # (1)!
```

1. Any string, parsed with MiniMessage


### Bucketable
Allows entity to be picked up by a bucket

```yaml
bucketable:
   bucketLiquidRequired: WATER # (1)!
   bucketItem: { prefab: myplugin:custom_bucket } # (2)!
```

1. Type of bucket player needs to be holding to pick up mob (WATER, LAVA)
2. The item to give when bucketing the mob
