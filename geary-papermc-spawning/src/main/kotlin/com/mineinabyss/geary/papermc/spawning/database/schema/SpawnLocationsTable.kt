package com.mineinabyss.geary.papermc.spawning.database.schema

import me.dvyy.sqlite.tables.Table

val SpawnLocationsTable = Table("""
CREATE TABLE IF NOT EXISTS SpawnLocationsTable (
    id INTEGER PRIMARY KEY,
    data TEXT
) STRICT;
""")
