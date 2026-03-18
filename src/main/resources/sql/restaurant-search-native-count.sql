SELECT COUNT(DISTINCT r.id)
FROM restaurants r
JOIN dishes d ON d.restaurant_id = r.id
WHERE (:city IS NULL OR LOWER(r.city) = LOWER(:city))
  AND (:cuisineType IS NULL OR LOWER(r.cuisine_type) = LOWER(:cuisineType))
  AND (:dishName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%')))
  AND (:minDishPrice IS NULL OR d.price >= :minDishPrice)
  AND (:maxDishPrice IS NULL OR d.price <= :maxDishPrice)
