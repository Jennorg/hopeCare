-- Tabla: rol
CREATE TABLE IF NOT EXISTS rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: persona
CREATE TABLE IF NOT EXISTS persona (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    apellido VARCHAR(150) NOT NULL,
    documento_identidad VARCHAR(50) UNIQUE,
    fecha_nacimiento DATE,
    telefono VARCHAR(50),
    email VARCHAR(200),
    direccion VARCHAR(300),
    genero VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: usuario
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(100) UNIQUE NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    id_persona INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol),
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: paciente
CREATE TABLE IF NOT EXISTS paciente (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    historia_clinica VARCHAR(100) UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo VARCHAR(20),
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo INT DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: especialidad
CREATE TABLE IF NOT EXISTS especialidad (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,
    nombre_especialidad VARCHAR(200) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: medico
CREATE TABLE IF NOT EXISTS medico (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    id_especialidad INT NOT NULL,
    registro_medico VARCHAR(100) UNIQUE NOT NULL,
    precio_consulta DECIMAL(10,2) DEFAULT 0.00,
    activo INT DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: horario_atencion
CREATE TABLE IF NOT EXISTS horario_atencion (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_medico INT NOT NULL,
    dia_semana INT NOT NULL,
    hora_inicio VARCHAR(10) NOT NULL,
    hora_fin VARCHAR(10) NOT NULL,
    intervalo_minutos INT DEFAULT 30,
    activo INT DEFAULT 1,
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: cita
CREATE TABLE IF NOT EXISTS cita (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_medico INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado VARCHAR(50) NOT NULL,
    motivo TEXT,
    creada_por INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico),
    FOREIGN KEY (creada_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: consulta
CREATE TABLE IF NOT EXISTS consulta (
    id_consulta INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE,
    diagnostico TEXT,
    sintomas TEXT,
    tratamiento TEXT,
    notas_medicas TEXT,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP,
    facturado INT DEFAULT 0,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: factura
CREATE TABLE IF NOT EXISTS factura (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    impuesto DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(50) NOT NULL,
    forma_pago VARCHAR(50),
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla: detalle_factura
CREATE TABLE IF NOT EXISTS detalle_factura (
    id_detalle_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    concepto VARCHAR(500) NOT NULL,
    id_referencia INT,
    tipo_referencia VARCHAR(50),
    monto DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
