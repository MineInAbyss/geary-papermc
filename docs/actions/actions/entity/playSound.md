---
template: configurable.html
title: playSound
type: Entity
desc: Plays a sound to a player
params:
  sound:
    type: String
    desc: The Minecraft key for the sound
  category:
    type: SoundCategory
    desc: The sound category in the volumes tab
    default: MASTER
  volume:
    type: Float
    desc: The volume of the sound
    default: 1
  pitch:
    type: Float
    desc: The pitch of the sound
    default: 1
---

## Examples

```yaml
playSound:
  sound: minecraft:entity.generic.extinguish_fire
```
