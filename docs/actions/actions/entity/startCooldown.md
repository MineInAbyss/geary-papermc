---
template: configurable.html
title: startCooldown
type: Any
desc: Starts a cooldown for an entity
params:
  length:
    type: Duration
    desc: The length of the cooldown
  display:
    type: ChatComponent
    desc: The actionbar message to display during the cooldown, if any
    default: "null"
  id:
    type: String
    desc: The ID of the cooldown to reference in conditions
---

## Examples

```yaml
startCooldown:
  id: flamethrower
  length: 2s
  display: <dark_aqua>Flamethrower
```
