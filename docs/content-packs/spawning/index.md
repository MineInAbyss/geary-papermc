# Spawning

Spawns are defined under `plugins/Geary/spawns/<namespace>`. Each file can contain multiple spawns, and within one file, entries may `inherit` other entries.

You can see a list of conditions for spawns at [Action system conditions](../../actions/conditions/index.md), any block condition will work here, as well as `mythicConditions` for conditions that aren't yet implemented by Geary.
{.tip}

A quick overview of most features by example:

```yaml
my_template:
  position: GROUND # We support GROUND, AIR, WATER
  priority: 1 # When multiple spawns are available, roll a weighted die with this value
  regions: [ layerone ] # WorldGuard regions to limit spawns to
  conditions: # Geary conditions from the action & observer system
    - mythicConditions:
        - lightlevelfromblocks{l=0}
    - maxNearby:
        amount: 3
  amount: 2-3 # Random range of mobs to spawn in a group
  spread: 7 # The horizontal spread for them
  ySpread: 1 # And the vertical spread

# Inherits all tags from the other config
my_mob_spawn:
  inherit: my_template
  type: mm:my_mythic_mob # Specifies a MythicMob to spawn, swap out mm for a namespace for Geary mobs
  conditions:
    - $inherit # Since we're overriding the `conditions` block, we need to manually specify we want to merge the lists
    - $remove maxNearby # Don't inherit maxNearby
    - blockBelow:
         allow: [ grass_block ]

my_mob_spawn_2: ...
```
