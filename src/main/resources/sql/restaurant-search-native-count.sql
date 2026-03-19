SELECT COUNT(DISTINCT r.id)
FROM restaurants r
JOIN dishes d ON d.restaurant_id = r.id
WHERE (:city IS NULL OR LOWER(r.city) = :city)
  AND (:cuisineType IS NULL OR LOWER(r.cuisine_type) = :cuisineType)
  AND (:dishNamePattern IS NULL OR LOWER(d.name) LIKE :dishNamePattern)
  AND (:minDishPrice IS NULL OR d.price >= :minDishPrice)
  AND (:maxDishPrice IS NULL OR d.price <= :maxDishPrice)
