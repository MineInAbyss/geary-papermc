package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.tables.Table

val SpawnLocationsRtree = Table(
    """
    CREATE VIRTUAL TABLE IF NOT EXISTS SpawnLocationsRtree USING rtree(
        id, minX, maxX, minY, maxY, minZ, maxZ,
    );
    """.trimIndent()
)
