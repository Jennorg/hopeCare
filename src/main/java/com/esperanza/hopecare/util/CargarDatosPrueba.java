package com.esperanza.hopecare.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CargarDatosPrueba {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insertar Roles
                insertarRol(conn, "ADMIN");
                insertarRol(conn, "RECEPCIONISTA");
                insertarRol(conn, "MEDICO");

                // Insertar Especialidades
                insertarEspecialidad(conn, "Medicina General");
                insertarEspecialidad(conn, "Pediatría");
                insertarEspecialidad(conn, "Traumatología");

                // Insertar Personas para Usuarios
                int pAdmin = insertarPersona(conn, "Admin", "Sistema", "00000001", null, null, "admin@hopecare.com", null, null);
                int pRecep = insertarPersona(conn, "Recep", "Sistema", "00000002", null, null, "recep@hopecare.com", null, null);
                int pMed = insertarPersona(conn, "Medico", "Sistema", "00000003", null, null, "medico@hopecare.com", null, null);

                // Insertar Usuarios (Contraseña simple para prueba)
                insertarUsuario(conn, "admin", "admin123", 1, pAdmin);
                insertarUsuario(conn, "recep", "recep123", 2, pRecep);
                insertarUsuario(conn, "medico", "medico123", 3, pMed);

                // Insertar Pacientes de prueba
                int pp1 = insertarPersona(conn, "Juan", "Pérez", "12345678", "1980-01-15", "123456789", "juan.perez@email.com", "Calle 123 #45-67", "M");
                insertarPaciente(conn, pp1, "HC001", "Ninguna", "O+", "María Pérez - 987654321");

                // Insertar Médico de prueba
                int pm1 = insertarPersona(conn, "Ana", "Martínez", "87654321", "1970-07-15", "678901234", "ana.martinez@email.com", "Calle 789 #12-34", "F");
                insertarMedico(conn, pm1, 1, "RM12345", 50000.0);

                conn.commit();
                System.out.println("Datos de prueba (incluyendo usuarios) insertados correctamente.");

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertarRol(Connection conn, String nombre) throws SQLException {
        String sql = "INSERT OR IGNORE INTO rol (nombre_rol) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        }
    }

    /**
     * Inserta un usuario en la base de datos vinculándolo a un rol y una persona.
     * La contraseña se guarda de forma plana para facilitar pruebas de desarrollo.
     */
    private static void insertarUsuario(Connection conn, String user, String pass, int idRol, int idPersona) throws SQLException {
        String sql = "INSERT OR IGNORE INTO usuario (nombre_usuario, contrasena, id_rol, id_persona) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setInt(3, idRol);
            ps.setInt(4, idPersona);
            ps.executeUpdate();
        }
    }

    private static void insertarEspecialidad(Connection conn, String nombre) throws SQLException {
        String sql = "INSERT OR IGNORE INTO especialidad (nombre_especialidad) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        }
    }

    private static int insertarPersona(Connection conn, String nombre, String apellido, String documento,
                                  String fechaNacimiento, String telefono, String email, String direccion, String genero) throws SQLException {
        String sql = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, documento);
            ps.setString(4, fechaNacimiento);
            ps.setString(5, telefono);
            ps.setString(6, email);
            ps.setString(7, direccion);
            ps.setString(8, genero);
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener id_persona");
        }
    }

    private static void insertarPaciente(Connection conn, int idPersona, String historiaClinica, String alergias, String grupoSanguineo, String contactoEmergencia) throws SQLException {
        String sql = "INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ps.setString(2, historiaClinica);
            ps.setString(3, alergias);
            ps.setString(4, grupoSanguineo);
            ps.setString(5, contactoEmergencia);
            ps.executeUpdate();
        }
    }

    private static void insertarMedico(Connection conn, int idPersona, int idEspecialidad, String registroMedico, double precioConsulta) throws SQLException {
        String sql = "INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ps.setInt(2, idEspecialidad);
            ps.setString(3, registroMedico);
            ps.setDouble(4, precioConsulta);
            ps.setInt(5, 1);
            ps.executeUpdate();
        }
    }
}
