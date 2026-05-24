-- Tabla: persona (tabla base para pacientes y médicos)
CREATE TABLE IF NOT EXISTS persona (
    id_persona INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    documento_identidad TEXT UNIQUE NOT NULL,
    fecha_nacimiento TEXT,
    telefono TEXT,
    email TEXT,
    direccion TEXT,
    genero TEXT
);

-- Tabla: paciente
CREATE TABLE IF NOT EXISTS paciente (
    id_paciente INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona INTEGER NOT NULL,
    historia_clinica TEXT UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo TEXT,
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo INTEGER DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

-- Tabla: especialidad
CREATE TABLE IF NOT EXISTS especialidad (
    id_especialidad INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_especialidad TEXT UNIQUE NOT NULL
);

-- Tabla: medico
CREATE TABLE IF NOT EXISTS medico (
    id_medico INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona INTEGER NOT NULL,
    id_especialidad INTEGER NOT NULL,
    registro_medico TEXT UNIQUE NOT NULL,
    fecha_contratacion TEXT,
    activo INTEGER DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad)
);
