-- Insertar datos iniciales solo si no existen (idempotente)
INSERT INTO posts (title, content, created_at, updated_at)
SELECT 'Bienvenido al blog', 'Este es el primer post de prueba en el sistema.', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = 'Bienvenido al blog');

INSERT INTO posts (title, content, created_at, updated_at)
SELECT 'Segundo artículo', 'Contenido de ejemplo para verificar inserciones.', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = 'Segundo artículo');

INSERT INTO posts (title, content, created_at, updated_at)
SELECT 'Tips de desarrollo', 'Aquí puedes escribir consejos sobre programación.', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = 'Tips de desarrollo');

INSERT INTO posts (title, content, created_at, updated_at)
SELECT 'Actualización del sistema', 'Se han realizado mejoras en el rendimiento.', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = 'Actualización del sistema');

INSERT INTO posts (title, content, created_at, updated_at)
SELECT 'Post vacío de contenido', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = 'Post vacío de contenido');