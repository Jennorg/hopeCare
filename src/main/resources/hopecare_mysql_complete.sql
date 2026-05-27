-- ==========================================================
-- SCRIPT COMPLETO: HopeCare MySQL (Base unificada)
-- Todas las tablas en hopecare_clinica para compatibilidad
-- con los DAOs existentes del módulo citas_médicas
-- ==========================================================

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

CREATE TABLE horario_atencion (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_medico INT NOT NULL,
    dia_semana INT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    intervalo_minutos INT DEFAULT 30,
    activo TINYINT(1) DEFAULT 1,
    FOREIGN KEY (id_medico) REFERENCES medico(id_medico)
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
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    facturado TINYINT(1) DEFAULT 0,
    FOREIGN KEY (id_cita) REFERENCES cita(id_cita)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- DATOS INICIALES
-- ----------------------------------------------------------
INSERT INTO rol (nombre_rol) VALUES ('ADMIN'), ('MEDICO'), ('RECEPCION');
INSERT INTO persona (nombre, apellido, documento_identidad, email)
VALUES ('Admin', 'Sistema', '00000000', 'admin@hopecare.com');
INSERT INTO especialidad (nombre_especialidad) VALUES
('Medicina General'), ('Pediatría'), ('Ginecología'), ('Cardiología'), ('Dermatología');
-- admin/admin123
INSERT INTO usuario (nombre_usuario, contrasena, contrasena_hash, id_rol, id_persona, rol)
VALUES ('admin', 'admin123', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 1, 1, 'ADMIN');
