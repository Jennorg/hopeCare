-- Tabla: rol
CREATE TABLE IF NOT EXISTS rol (
    id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_rol TEXT UNIQUE NOT NULL
);

-- Tabla: usuario
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT UNIQUE NOT NULL,
    contrasena TEXT NOT NULL,
    contrasena_hash TEXT,
    id_rol INTEGER,
    id_persona INTEGER NOT NULL,
    rol TEXT NOT NULL DEFAULT 'MEDICO',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);
