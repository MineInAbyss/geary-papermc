package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.Geary

class GearyPaper(
    val plugin: GearyPlugin,
    world: Geary,
) : Geary by world
