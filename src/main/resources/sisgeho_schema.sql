-- ======================================================
-- Sistema de Gestión Hospitalaria (Sisgeho) - Módulo Pacientes y Médicos
-- Base de datos para MySQL
-- ======================================================

CREATE DATABASE IF NOT EXISTS hopecare_clinica;
USE hopecare_clinica;

-- Tabla: rol
CREATE TABLE IF NOT EXISTS rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

-- Tabla: persona (tabla base para pacientes y médicos)
CREATE TABLE IF NOT EXISTS persona (
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

-- Tabla: usuario
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    contrasena_hash VARCHAR(255),
    id_rol INT NOT NULL,
    id_persona INT NOT NULL,
    rol VARCHAR(50) DEFAULT 'MEDICO',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol),
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

-- Tabla: paciente
CREATE TABLE IF NOT EXISTS paciente (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    id_persona INT NOT NULL,
    historia_clinica VARCHAR(50) UNIQUE NOT NULL,
    alergias TEXT,
    grupo_sanguineo VARCHAR(10),
    contacto_emergencia TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

-- Tabla: especialidad
CREATE TABLE IF NOT EXISTS especialidad (
    id_especialidad INT AUTO_INCREMENT PRIMARY KEY,
    nombre_especialidad VARCHAR(100) UNIQUE NOT NULL
) ENGINE=InnoDB;

-- Tabla: medico
CREATE TABLE IF NOT EXISTS medico (
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
