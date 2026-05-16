-- ======================================================
-- Sistema de Gestión Hospitalaria (Sisgeho)
-- Base de datos para SQLite
-- ======================================================

-- Tabla: rol
CREATE TABLE rol (
    id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_rol TEXT UNIQUE NOT NULL,
    descripcion TEXT
);

-- Tabla: usuario
CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT UNIQUE NOT NULL,
    contrasena_hash TEXT NOT NULL,
    id_rol INTEGER NOT NULL,
    activo INTEGER DEFAULT 1,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- Tabla: persona (tabla base para pacientes y médicos)
CREATE TABLE persona (
    id_persona INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_persona TEXT NOT NULL,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    documento_identidad TEXT UNIQUE,
    fecha_nacimiento DATE,
    telefono TEXT,
    email TEXT,
    direccion TEXT,
    genero TEXT,
    id_usuario INTEGER,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- Tabla: paciente
CREATE TABLE paciente (
    id_paciente INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona INTEGER NOT NULL,
    historia_clinica TEXT UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo TEXT,
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

-- Tabla: especialidad
CREATE TABLE especialidad (
    id_especialidad INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_especialidad TEXT UNIQUE NOT NULL,
    descripcion TEXT
);

-- Tabla: medico
CREATE TABLE medico (
    id_medico INTEGER PRIMARY KEY AUTOINCREMENT,
    id_persona INTEGER NOT NULL,
    id_especialidad INTEGER NOT NULL,
    registro_medico TEXT UNIQUE NOT NULL,
    activo INTEGER DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad)
);

-- Tabla: horario_atencion
CREATE TABLE horario_atencion (
    id_horario INTEGER PRIMARY KEY AUTOINCREMENT,
    id_medico INTEGER NOT NULL,
    dia_semana INTEGER NOT NULL,
    hora_inicio TEXT NOT NULL,
    hora_fin TEXT NOT NULL,
    intervalo_minutos INTEGER DEFAULT 30,
    activo INTEGER DEFAULT 1,
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico)
);

-- Tabla: cita
CREATE TABLE cita (
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

-- Tabla: consulta
CREATE TABLE consulta (
    id_consulta INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cita INTEGER NOT NULL UNIQUE,
    diagnostico TEXT,
    sintomas TEXT,
    tratamiento TEXT,
    notas_medicas TEXT,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP,
    facturado INTEGER DEFAULT 0,
    precio REAL NOT NULL DEFAULT 0.0,
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
);

-- Tabla: medicamento
CREATE TABLE medicamento (
    id_medicamento INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_comercial TEXT NOT NULL,
    principio_activo TEXT,
    presentacion TEXT,
    concentracion TEXT,
    precio_unitario REAL NOT NULL,
    stock_actual INTEGER DEFAULT 0,
    stock_minimo INTEGER DEFAULT 0,
    requiere_receta INTEGER DEFAULT 1
);

-- Tabla: entrega_medicamento (sin recetas, referencia directa a paciente)
CREATE TABLE entrega_medicamento (
    id_entrega INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    id_medicamento INTEGER NOT NULL,
    cantidad_entregada INTEGER NOT NULL,
    presente_receta INTEGER DEFAULT 0,
    fecha_entrega DATETIME DEFAULT CURRENT_TIMESTAMP,
    entregado_por INTEGER NOT NULL,
    facturado INTEGER DEFAULT 0,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_medicamento) REFERENCES medicamento(id_medicamento),
    FOREIGN KEY (entregado_por) REFERENCES usuario(id_usuario)
);

-- Tabla: examen_laboratorio
CREATE TABLE examen_laboratorio (
    id_examen INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_examen TEXT UNIQUE NOT NULL,
    descripcion TEXT,
    precio REAL NOT NULL,
    tiempo_resultado_horas INTEGER,
    resultado_archivo BLOB
);

-- Tabla: solicitud_examen (sin consulta, referencia directa a paciente)
CREATE TABLE solicitud_examen (
    id_solicitud INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    id_examen INTEGER NOT NULL,
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE',
    resultado_texto TEXT,
    resultado_archivo BLOB,
    realizado_por INTEGER,
    facturado INTEGER DEFAULT 0,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_examen) REFERENCES examen_laboratorio(id_examen),
    FOREIGN KEY (realizado_por) REFERENCES usuario(id_usuario)
);

-- Tabla: factura
CREATE TABLE factura (
    id_factura INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal REAL NOT NULL,
    impuesto REAL DEFAULT 0,
    total REAL NOT NULL,
    estado_pago TEXT NOT NULL,
    forma_pago TEXT,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente)
);

-- Tabla: detalle_factura
CREATE TABLE detalle_factura (
    id_detalle_factura INTEGER PRIMARY KEY AUTOINCREMENT,
    id_factura INTEGER NOT NULL,
    concepto TEXT NOT NULL,
    id_referencia INTEGER,
    tipo_referencia TEXT,
    monto REAL NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
);

-- Tabla: bitacora_eventos
CREATE TABLE bitacora_eventos (
    id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    tabla_afectada TEXT,
    id_registro INTEGER,
    accion TEXT,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    datos_antes TEXT,
    datos_despues TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);