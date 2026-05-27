-- ==========================================================
-- SCRIPT DE MIGRACIÓN: MULTI-DATABASE (MySQL)
-- Simula la estructura original de SQLite con 5 bases de datos
-- ==========================================================

-- ----------------------------------------------------------
-- 1. BASE DE DATOS: hopecare_clinica (Datos maestros)
-- ----------------------------------------------------------
DROP DATABASE IF EXISTS hopecare_clinica;
CREATE DATABASE hopecare_clinica;
USE hopecare_clinica;

CREATE TABLE persona (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    fecha_nacimiento DATE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    genero VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE especialidad (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,
    nombre_especialidad VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE medico (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    id_especialidad INT NOT NULL,
    registro_medico VARCHAR(50) UNIQUE NOT NULL,
    precio_consulta DECIMAL(10,2) DEFAULT 0.0,
    fecha_contratacion DATE,
    activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad)
) ENGINE=InnoDB;

CREATE TABLE paciente (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    historia_clinica VARCHAR(50) UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo VARCHAR(5),
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 2. BASE DE DATOS: hopecare_auth (Seguridad)
-- ----------------------------------------------------------
DROP DATABASE IF EXISTS hopecare_auth;
CREATE DATABASE hopecare_auth;
USE hopecare_auth;

CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    contrasena_hash VARCHAR(255),
    id_rol INT,
    id_persona INT NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'MEDICO',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 3. BASE DE DATOS: hopecare_citas (Operaciones)
-- ----------------------------------------------------------
DROP DATABASE IF EXISTS hopecare_citas;
CREATE DATABASE hopecare_citas;
USE hopecare_citas;

CREATE TABLE horario_atencion (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_medico INT NOT NULL,
    dia_semana INT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    intervalo_minutos INT DEFAULT 30,
    activo TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE cita (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_medico INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    motivo TEXT,
    creada_por INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE consulta (
    id_consulta INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE,
    diagnostico TEXT,
    sintomas TEXT,
    tratamiento TEXT,
    notas_medicas TEXT,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    facturado TINYINT(1) DEFAULT 0
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 4. BASE DE DATOS: hopecare_facturacion
-- ----------------------------------------------------------
DROP DATABASE IF EXISTS hopecare_facturacion;
CREATE DATABASE hopecare_facturacion;
USE hopecare_facturacion;

CREATE TABLE factura (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    impuesto DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL,
    forma_pago VARCHAR(50)
) ENGINE=InnoDB;

CREATE TABLE detalle_factura (
    id_detalle_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    id_referencia INT,
    tipo_referencia VARCHAR(50),
    monto DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 5. BASE DE DATOS: hopecare_dashboard (Espejo para analítica)
-- ----------------------------------------------------------
DROP DATABASE IF EXISTS hopecare_dashboard;
CREATE DATABASE hopecare_dashboard;
USE hopecare_dashboard;

-- Tablas necesarias para el DashboardDAO
CREATE TABLE persona (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE,
    fecha_nacimiento DATE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    genero VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    id_persona INT NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'MEDICO',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol),
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

CREATE TABLE especialidad (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,
    nombre_especialidad VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE medico (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    id_especialidad INT NOT NULL,
    registro_medico VARCHAR(50) UNIQUE NOT NULL,
    precio_consulta DECIMAL(10,2) DEFAULT 0.0,
    activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad)
) ENGINE=InnoDB;

CREATE TABLE paciente (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    historia_clinica VARCHAR(50) UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo VARCHAR(5),
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

CREATE TABLE cita (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_medico INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    motivo TEXT,
    creada_por INT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico),
    FOREIGN KEY (creada_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE consulta (
    id_consulta INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE,
    diagnostico TEXT,
    sintomas TEXT,
    tratamiento TEXT,
    notas_medicas TEXT,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP,
    facturado TINYINT(1) DEFAULT 0,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
) ENGINE=InnoDB;

CREATE TABLE medicamento (
    id_medicamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial VARCHAR(100) NOT NULL,
    principio_activo VARCHAR(100),
    presentacion VARCHAR(50),
    concentracion VARCHAR(50),
    precio_unitario DECIMAL(10,2) NOT NULL,
    stock_actual INT DEFAULT 0,
    stock_minimo INT DEFAULT 0,
    requiere_receta TINYINT(1) DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE factura (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    impuesto DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL,
    forma_pago VARCHAR(50),
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente)
) ENGINE=InnoDB;

CREATE TABLE detalle_factura (
    id_detalle_factura INT AUTO_INCREMENT PRIMARY KEY,
    id_factura INT NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    id_referencia INT,
    tipo_referencia VARCHAR(50),
    monto DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- DATOS INICIALES
-- ----------------------------------------------------------
USE hopecare_auth;
INSERT INTO rol (nombre_rol) VALUES ('ADMIN'), ('MEDICO'), ('RECEPCION');
-- Contraseña 'admin123' hashed (8c6976e5...)
INSERT INTO usuario (nombre_usuario, contrasena, contrasena_hash, id_rol, id_persona, rol) 
VALUES ('admin', 'admin123', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 1, 1, 'ADMIN');

USE hopecare_clinica;
INSERT INTO especialidad (nombre_especialidad) VALUES 
('Medicina General'), ('Pediatría'), ('Ginecología'), ('Cardiología'), ('Dermatología');
INSERT INTO persona (nombre, apellido, documento_identidad, email) 
VALUES ('Admin', 'Sistema', '00000000', 'admin@hopecare.com');
