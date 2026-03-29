INSERT INTO posts (title, content, created_at, updated_at)
VALUES ('Bienvenido al blog', 'Este es el primer post de prueba en el sistema.', NOW(), NOW()),
       ('Segundo artículo', 'Contenido de ejemplo para verificar inserciones.', NOW(), NOW()),
       ('Tips de desarrollo', 'Aquí puedes escribir consejos sobre programación.', NOW(), NOW()),
       ('Actualización del sistema', 'Se han realizado mejoras en el rendimiento.', NOW(), NOW()),
       ('Post vacío de contenido', NULL, NOW(), NOW());