-- Tabla: horario_atencion
CREATE TABLE IF NOT EXISTS horario_atencion (
    id_horario INTEGER PRIMARY KEY AUTOINCREMENT,
    id_medico INTEGER NOT NULL,
    dia_semana TEXT NOT NULL, -- Lunes, Martes...
    hora_inicio TEXT NOT NULL,
    hora_fin TEXT NOT NULL,
    activo INTEGER DEFAULT 1
);

-- Tabla: cita
CREATE TABLE IF NOT EXISTS cita (
    id_cita INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    id_medico INTEGER NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado TEXT NOT NULL,
    motivo TEXT,
    creada_por INTEGER NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: consulta
CREATE TABLE IF NOT EXISTS consulta (
    id_consulta INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cita INTEGER NOT NULL UNIQUE,
    diagnostico TEXT,
    sintomas TEXT,
    tratamiento TEXT,
    notas_medicas TEXT,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP,
    precio REAL NOT NULL DEFAULT 0.0
);
