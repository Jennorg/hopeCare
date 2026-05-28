package com.esperanza.hopecare.common.db;

import com.esperanza.hopecare.common.utils.Hasher;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CargarDatosPrueba {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                insertarRol(conn, "ADMIN");
                insertarRol(conn, "RECEPCIONISTA");
                insertarRol(conn, "MEDICO");

                insertarEspecialidad(conn, "Medicina General");
                insertarEspecialidad(conn, "Pediatría");
                insertarEspecialidad(conn, "Cardiología");

                int pAdmin = insertarPersona(conn, "Admin", "Sistema", "00000001", null, null, "admin@hopecare.com", null, null);
                int pRecep = insertarPersona(conn, "Recep", "Sistema", "00000002", null, null, "recep@hopecare.com", null, null);
                int pMed = insertarPersona(conn, "Carlos", "López", "00000003", "1985-03-15", "5551234567", "carlos.lopez@hopecare.com", "Av. Siempre Viva 742", "M");

                insertarUsuario(conn, "admin", "admin123", 1, pAdmin, "ADMIN");
                insertarUsuario(conn, "recep", "recep123", 2, pRecep, "RECEPCIONISTA");
                insertarUsuario(conn, "medico", "medico123", 3, pMed, "MEDICO");

                int pp1 = insertarPersona(conn, "Juan", "Pérez", "12345678", "1980-01-15", "123456789", "juan.perez@email.com", "Calle 123 #45-67", "M");
                int pp2 = insertarPersona(conn, "María", "González", "23456789", "1985-05-20", "234567890", "maria.gonzalez@email.com", "Carrera 45 #67-89", "F");
                int pp3 = insertarPersona(conn, "Pedro", "Ramírez", "34567890", "1978-11-10", "345678901", "pedro.ramirez@email.com", "Av. Principal #12-34", "M");
                int pp4 = insertarPersona(conn, "Laura", "Fernández", "45678901", "1990-03-25", "456789012", "laura.fernandez@email.com", "Calle 8 #90-12", "F");
                int pp5 = insertarPersona(conn, "Roberto", "Díaz", "56789012", "1975-09-08", "567890123", "roberto.diaz@email.com", "Calle 10 #11-22", "M");
                insertarPaciente(conn, pp1, "HC001", "Ninguna", "O+", "María Pérez - 987654321");
                insertarPaciente(conn, pp2, "HC002", "Penicilina", "A+", "Pedro González - 234567891");
                insertarPaciente(conn, pp3, "HC003", "Ninguna", "B+", "Ana Ramírez - 345678902");
                insertarPaciente(conn, pp4, "HC004", "Ibuprofeno", "AB+", "Carlos Fernández - 456789013");
                insertarPaciente(conn, pp5, "HC005", "Ninguna", "O-", "Sofía Díaz - 567890124");

                int pm1 = insertarPersona(conn, "Ana", "Martínez", "87654321", "1970-07-15", "678901234", "ana.martinez@email.com", "Calle 789 #12-34", "F");
                int idMed1 = insertarMedico(conn, pm1, 1, "RM12345", 50000.0);

                int idMed2 = insertarMedico(conn, pMed, 1, "RM99998", 55000.0);
                int pm3 = insertarPersona(conn, "Sofía", "Torres", "11111111", "1982-04-18", "1112223333", "sofia.torres@email.com", "Calle 50 #20-30", "F");
                int idMed3 = insertarMedico(conn, pm3, 2, "RM77777", 60000.0);

                for (int medId : new int[]{idMed1, idMed2, idMed3}) {
                    for (int dia = 1; dia <= 5; dia++) {
                        insertarHorario(conn, medId, dia, "08:00", "12:00", 30);
                    }
                }

                insertarCitas(conn);
                insertarConsultas(conn);

                conn.commit();
                System.out.println("Datos de prueba insertados correctamente.");
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

    private static void insertarCitas(Connection conn) throws SQLException {
        String sql = "INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, creada_por) VALUES (?, ?, ?, ?, ?)";
        LocalDate today = LocalDate.now();
        LocalDate[][] fechas = {
            {today.minusDays(1), today.minusDays(1), today.minusDays(1)},
            {today.minusDays(1), today.minusDays(1)},
            {today.plusDays(1), today.plusDays(1)},
            {today.plusDays(2), today.plusDays(2), today.plusDays(2)}
        };
        String[] estados = {"ATENDIDA", "ATENDIDA", "ATENDIDA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA"};
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 0;
            for (int g = 0; g < fechas.length; g++) {
                for (int i = 0; i < fechas[g].length; i++) {
                    LocalDateTime dt = LocalDateTime.of(fechas[g][i], LocalTime.of(8 + (idx % 3) * 2, 0));
                    ps.setInt(1, (idx % 5) + 1);
                    ps.setInt(2, (idx % 3) + 1);
                    ps.setString(3, dt.format(DT_FMT));
                    ps.setString(4, estados[idx]);
                    ps.setInt(5, 1);
                    ps.executeUpdate();
                    idx++;
                }
            }
        }
    }

    private static void insertarConsultas(Connection conn) throws SQLException {
        String sql = "INSERT INTO consulta (id_cita, diagnostico, sintomas, tratamiento, precio) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 1); ps.setString(2, "Gripe"); ps.setString(3, "Fiebre, tos"); ps.setString(4, "Reposo y analgesicos"); ps.setDouble(5, 50000.0); ps.executeUpdate();
            ps.setInt(1, 2); ps.setString(2, "Revision sin novedades"); ps.setString(3, "Asintomatico"); ps.setString(4, "Ninguno"); ps.setDouble(5, 45000.0); ps.executeUpdate();
            ps.setInt(1, 3); ps.setString(2, "Control anual normal"); ps.setString(3, "Fatiga leve"); ps.setString(4, "Ejercicio moderado"); ps.setDouble(5, 80000.0); ps.executeUpdate();
        }
    }
}
