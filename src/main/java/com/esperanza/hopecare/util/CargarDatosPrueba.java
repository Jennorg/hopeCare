package com.esperanza.hopecare.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CargarDatosPrueba {

    public static void main(String[] args) {
        try (Connection connClinica = DatabaseConnection.getClinicaConnection();
             Connection connAuth = DatabaseConnection.getAuthConnection();
             Connection connCitas = DatabaseConnection.getCitasConnection()) {
            
            connClinica.setAutoCommit(false);
            connAuth.setAutoCommit(false);
            connCitas.setAutoCommit(false);
            
            try {
                // 1. Insertar Roles (Auth)
                insertarRol(connAuth, "ADMIN");
                insertarRol(connAuth, "RECEPCIONISTA");
                insertarRol(connAuth, "MEDICO");

                // 2. Insertar Especialidades (Clinica)
                insertarEspecialidad(connClinica, "Medicina General");
                insertarEspecialidad(connClinica, "Pediatría");
                insertarEspecialidad(connClinica, "Traumatología");

                // 3. Insertar Personas para Usuarios (Clinica)
                int pAdmin = insertarPersona(connClinica, "Admin", "Sistema", "00000001", null, null, "admin@hopecare.com", null, null);
                int pRecep = insertarPersona(connClinica, "Recep", "Sistema", "00000002", null, null, "recep@hopecare.com", null, null);
                int pMed = insertarPersona(connClinica, "Carlos", "López", "00000003", "1985-03-15", "5551234567", "carlos.lopez@hopecare.com", "Av. Siempre Viva 742", "M");

                // 4. Insertar Usuarios (Auth)
                insertarUsuario(connAuth, "admin", "admin123", 1, pAdmin, "ADMIN");
                insertarUsuario(connAuth, "recep", "recep123", 2, pRecep, "RECEPCIONISTA");
                insertarUsuario(connAuth, "medico", "medico123", 3, pMed, "MEDICO");

                // 5. Insertar Pacientes de prueba (Clinica)
                int pp1 = insertarPersona(connClinica, "Juan", "Pérez", "12345678", "1980-01-15", "123456789", "juan.perez@email.com", "Calle 123 #45-67", "M");
                insertarPaciente(connClinica, pp1, "HC001", "Ninguna", "O+", "María Pérez - 987654321");

                // 6. Insertar Médico de prueba (Clinica)
                int pm1 = insertarPersona(connClinica, "Ana", "Martínez", "87654321", "1970-07-15", "678901234", "ana.martinez@email.com", "Calle 789 #12-34", "F");
                int idMed1 = insertarMedico(connClinica, pm1, 1, "RM12345", 50000.0);

                // 7. Crear médico real para el usuario 'medico/medico123' (id_persona=3)
                //    para que pueda tener citas asignadas y hacer consultas
                int idMed2 = insertarMedico(connClinica, pMed, 1, "RM99998", 55000.0);

                // 8. Insertar Horarios para el médico 1 - Ana Martínez (Citas)
                insertarHorario(connCitas, idMed1, 1, "08:00", "12:00", 30); // Lunes
                insertarHorario(connCitas, idMed1, 2, "08:00", "12:00", 30); // Martes
                insertarHorario(connCitas, idMed1, 3, "08:00", "12:00", 30); // Miércoles
                insertarHorario(connCitas, idMed1, 4, "08:00", "12:00", 30); // Jueves
                insertarHorario(connCitas, idMed1, 5, "08:00", "12:00", 30); // Viernes
                insertarHorario(connCitas, idMed1, 6, "08:00", "12:00", 30); // Sábado
                insertarHorario(connCitas, idMed1, 7, "08:00", "12:00", 30); // Domingo

                // 9. Insertar Horarios para el médico 2 - Medico Sistema (usuario medico/medico123)
                insertarHorario(connCitas, idMed2, 1, "08:00", "12:00", 30);
                insertarHorario(connCitas, idMed2, 2, "08:00", "12:00", 30);
                insertarHorario(connCitas, idMed2, 3, "08:00", "12:00", 30);
                insertarHorario(connCitas, idMed2, 4, "08:00", "12:00", 30);
                insertarHorario(connCitas, idMed2, 5, "08:00", "12:00", 30);
                insertarHorario(connCitas, idMed2, 6, "08:00", "12:00", 30);
                insertarHorario(connCitas, idMed2, 7, "08:00", "12:00", 30);

                connClinica.commit();
                connAuth.commit();
                connCitas.commit();

            } catch (SQLException e) {
                connClinica.rollback();
                connAuth.rollback();
                connCitas.rollback();
                e.printStackTrace();
            } finally {
                connClinica.setAutoCommit(true);
                connAuth.setAutoCommit(true);
                connCitas.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertarRol(Connection conn, String nombre) throws SQLException {
        String sql = "INSERT IGNORE INTO rol (nombre_rol) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        }
    }

    private static void insertarUsuario(Connection conn, String user, String pass, int idRol, int idPersona, String nombreRol) throws SQLException {
        String sql = "INSERT IGNORE INTO usuario (nombre_usuario, contrasena, contrasena_hash, id_rol, id_persona, rol) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, Hasher.hash(pass));
            ps.setInt(4, idRol);
            ps.setInt(5, idPersona);
            ps.setString(6, nombreRol);
            ps.executeUpdate();
        }
    }

    private static void insertarEspecialidad(Connection conn, String nombre) throws SQLException {
        String sql = "INSERT IGNORE INTO especialidad (nombre_especialidad) VALUES (?)";
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

    private static void insertarPaciente(Connection conn, int idPersona, String historiaClinica, String allergic, String grupoSanguineo, String contactoEmergencia) throws SQLException {
        String sql = "INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ps.setString(2, historiaClinica);
            ps.setString(3, allergic);
            ps.setString(4, grupoSanguineo);
            ps.setString(5, contactoEmergencia);
            ps.executeUpdate();
        }
    }

    private static int insertarMedico(Connection conn, int idPersona, int idEspecialidad, String registroMedico, double precioConsulta) throws SQLException {
        String sql = "INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPersona);
            ps.setInt(2, idEspecialidad);
            ps.setString(3, registroMedico);
            ps.setDouble(4, precioConsulta);
            ps.setInt(5, 1);
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener id_medico");
        }
    }

    private static void insertarHorario(Connection conn, int idMedico, int diaSemana, String inicio, String fin, int intervalo) throws SQLException {
        String sql = "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setInt(2, diaSemana);
            ps.setString(3, inicio);
            ps.setString(4, fin);
            ps.setInt(5, intervalo);
            ps.setInt(6, 1);
            ps.executeUpdate();
        }
    }
}
