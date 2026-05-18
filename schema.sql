-- ======================================================
-- Sistema de Gestión Hospitalaria (Sisgeho)
-- Base de datos para SQLite
-- Generado a partir de DBML
-- ======================================================

-- Tabla: rol
CREATE TABLE rol (
    id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_rol TEXT UNIQUE NOT NULL
);

-- Tabla: usuario
CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT UNIQUE NOT NULL,
    contrasena_hash TEXT NOT NULL,
    id_rol INTEGER NOT NULL,
    id_persona INTEGER NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol),
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

-- Tabla: persona (tabla base para pacientes y médicos)
CREATE TABLE persona (
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
    nombre_especialidad TEXT UNIQUE NOT NULL
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
    dia_semana INTEGER NOT NULL, -- 1=Lunes..7=Domingo
    hora_inicio TEXT NOT NULL, -- HH:MM
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
    estado TEXT NOT NULL, -- PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA, NO_ASISTIO
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
    requiere_receta INTEGER DEFAULT 1
);

-- Tabla: receta
CREATE TABLE receta (
    id_receta INTEGER PRIMARY KEY AUTOINCREMENT,
    id_consulta INTEGER NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    instrucciones TEXT,
    activa INTEGER DEFAULT 1,
    FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta)
);

-- Tabla: detalle_receta
CREATE TABLE detalle_receta (
    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
    id_receta INTEGER NOT NULL,
    id_medicamento INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    dosis_indicacion TEXT,
    FOREIGN KEY (id_receta) REFERENCES receta(id_receta),
    FOREIGN KEY (id_medicamento) REFERENCES medicamento(id_medicamento)
);

-- Tabla: entrega_medicamento
CREATE TABLE entrega_medicamento (
    id_entrega INTEGER PRIMARY KEY AUTOINCREMENT,
    id_detalle_receta INTEGER NOT NULL,
    cantidad_entregada INTEGER NOT NULL,
    fecha_entrega DATETIME DEFAULT CURRENT_TIMESTAMP,
    entregado_por INTEGER NOT NULL,
    FOREIGN KEY (id_detalle_receta) REFERENCES detalle_receta(id_detalle),
    FOREIGN KEY (entregado_por) REFERENCES usuario(id_usuario)
);

-- Tabla: examen_laboratorio
CREATE TABLE examen_laboratorio (
    id_examen INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_examen TEXT UNIQUE NOT NULL,
    descripcion TEXT,
    precio REAL NOT NULL,
    tiempo_resultado_horas INTEGER
);

-- Tabla: solicitud_examen
CREATE TABLE solicitud_examen (
    id_solicitud INTEGER PRIMARY KEY AUTOINCREMENT,
    id_consulta INTEGER NOT NULL,
    id_examen INTEGER NOT NULL,
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, REALIZADO, CANCELADO
    resultado_texto TEXT,
    resultado_archivo BLOB,
    realizado_por INTEGER,
    FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta),
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
    estado_pago TEXT NOT NULL, -- PENDIENTE, PAGADO, ANULADO
    forma_pago TEXT,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente)
);

-- Tabla: detalle_factura
CREATE TABLE detalle_factura (
    id_detalle_factura INTEGER PRIMARY KEY AUTOINCREMENT,
    id_factura INTEGER NOT NULL,
    concepto TEXT NOT NULL,
    id_referencia INTEGER,
    tipo_referencia TEXT, -- CONSULTA, EXAMEN, MEDICAMENTO
    monto REAL NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
);

-- Tabla: bitacora_eventos (opcional)
CREATE TABLE bitacora_eventos (
    id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    tabla_afectada TEXT,
    id_registro INTEGER,
    accion TEXT, -- INSERT, UPDATE, DELETE
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    datos_antes TEXT,
    datos_despues TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);
