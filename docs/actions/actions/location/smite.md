---
template: configurable.html
title: smite
type: Location
desc: Strikes lightning at a location
params:
  at:
    type: Location
    desc: Where to strike the lightning
---

## Examples

```yaml
smite:
  at: "{{ targetBlock }}"
```
