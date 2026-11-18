INSERT INTO workers (user_id)
SELECT id
FROM users
WHERE username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM workers
      WHERE workers.user_id = users.id
  );
