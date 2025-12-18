-- getSpawnsNear
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
ORDER BY abs(minX - :x) + abs(minY - :y) + abs(minZ - :z);

-- countSpawnsInBB
SELECT count(*) AS count
FROM spawn_rtree
WHERE minX >= :minX
  AND minY >= :minY
  AND minZ >= :minZ
  AND maxX < :maxX
  AND maxY < :maxY
  AND maxZ < :maxZ;

-- countSpawnsInBBOfType
SELECT count(*) AS count
FROM spawn_rtree
WHERE minX >= :minX
  AND minY >= :minY
  AND minZ >= :minZ
  AND maxX < :maxX
  AND maxY < :maxY
  AND maxZ < :maxZ
  AND minCategory IN ( SELECT id FROM spawn_categories WHERE name IN ( SELECT value FROM json_each(:categories) ) );

-- countNearby
SELECT count(*) AS count
FROM spawn_rtree
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
  AND (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z) <= :rad * :rad;

-- countNearbyOfType
SELECT count(*) AS count
FROM spawn_rtree
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
  AND (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z) <= :rad * :rad
  AND minCategory IN ( SELECT id FROM spawn_categories WHERE name IN ( SELECT value FROM json_each(:categories) ) );

-- getClosestSpawn
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
ORDER BY (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z)
LIMIT 1;

-- getClosestSpawnOfType
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX > :x - :rad
  AND minY > :y - :rad
  AND minZ > :z - :rad
  AND maxX < :x + :rad
  AND maxY < :y + :rad
  AND maxZ < :z + :rad
  AND minCategory IN ( SELECT id FROM spawn_categories WHERE name IN ( SELECT value FROM json_each(:categories) ) )
ORDER BY (minX - :x) * (minX - :x) + (minY - :y) * (minY - :y) + (minZ - :z) * (minZ - :z)
LIMIT 1;

-- getSpawnsInBB
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX >= :minX
  AND minY >= :minY
  AND minZ >= :minZ
  AND maxX < :maxX
  AND maxY < :maxY
  AND maxZ < :maxZ;

-- getSpawnsInChunk
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX >= :x
  AND minZ >= :z
  AND maxX < :x + 16
  AND maxZ < :z + 16;

-- getSpawnsInChunkOfType
SELECT id, data, minX, minY, minZ
FROM spawn_view
WHERE minX >= :x
  AND minZ >= :z
  AND maxX < :x + 16
  AND maxZ < :z + 16
  AND minCategory IN ( SELECT id FROM spawn_categories WHERE name IN ( SELECT value FROM json_each(:categories) ) );

-- createCategoriesIfMissing
INSERT OR IGNORE INTO spawn_categories(name)
SELECT value
FROM json_each(:categories);

-- insertData
INSERT INTO spawn_data(id, data)
VALUES (:id, json(:data));


-- insertRtree
INSERT INTO spawn_rtree(minX, maxX, minY, maxY, minZ, maxZ)
VALUES (:x, :x, :y, :y, :z, :z)
RETURNING id;

-- deleteSpawnsOlderThan
DELETE
FROM spawn_data
WHERE data ->> 'createdTime' < :epochSeconds;

-- deleteSpawn
DELETE
FROM spawn_data
WHERE id = :id;

-- dropAll
DELETE
FROM spawn_data;