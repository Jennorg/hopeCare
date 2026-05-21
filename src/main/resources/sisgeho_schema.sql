CREATE TABLE IF NOT EXISTS persona (
    id_persona INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    documento_identidad TEXT UNIQUE,
    fecha_nacimiento DATE,
    telefono TEXT,
    email TEXT,
    direccion TEXT,
    genero TEXT
);

CREATE TABLE IF NOT EXISTS especialidad (
    id_especialidad INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_especialidad TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS medico (
    id_medico INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona INTEGER NOT NULL UNIQUE,
    id_especialidad INTEGER NOT NULL,
    registro_medico TEXT UNIQUE NOT NULL,
    precio_consulta REAL DEFAULT 0.0,
    activo INTEGER DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad)
);

CREATE TABLE IF NOT EXISTS paciente (
    id_paciente INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona INTEGER NOT NULL UNIQUE,
    historia_clinica TEXT UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo TEXT,
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

CREATE TABLE IF NOT EXISTS horario_atencion (
    id_horario INTEGER PRIMARY KEY AUTOINCREMENT,
    id_medico INTEGER NOT NULL,
    dia_semana INTEGER NOT NULL,
    hora_inicio TEXT NOT NULL,
    hora_fin TEXT NOT NULL,
    intervalo_minutos INTEGER DEFAULT 30,
    activo INTEGER DEFAULT 1,
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico)
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT UNIQUE NOT NULL,
    contrasena_hash TEXT NOT NULL,
    id_persona INTEGER NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

CREATE TABLE IF NOT EXISTS cita (
    id_cita INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    id_medico INTEGER NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado TEXT NOT NULL,
    motivo TEXT,
    creada_por INTEGER NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico),
    FOREIGN KEY (creada_por) REFERENCES usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS consulta (
    id_consulta INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cita INTEGER NOT NULL UNIQUE,
    diagnostico TEXT,
    sintomas TEXT,
    tratamiento TEXT,
    notas_medicas TEXT,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP,
    precio REAL NOT NULL DEFAULT 0.0,
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
);
