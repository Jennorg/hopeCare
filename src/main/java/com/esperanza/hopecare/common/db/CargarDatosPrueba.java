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
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            insertarEspecialidades(conn);
            insertarPersonas(conn);
            insertarMedicos(conn);
            insertarPacientes(conn);
            insertarHorarios(conn);
            insertarUsuarios(conn);
            insertarCitas(conn);
            insertarConsultas(conn);

            conn.commit();
            System.out.println("Datos de prueba insertados correctamente.");
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private static void insertarEspecialidades(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO especialidad (nombre_especialidad) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String esp : new String[]{"Medicina General", "Pediatria", "Cardiologia"}) {
                ps.setString(1, esp);
                ps.executeUpdate();
            }
        }
    }

    private static void insertarPersonas(Connection conn) throws SQLException {
        String sql = "INSERT INTO persona (nombre, apellido, documento_identidad) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Ana"); ps.setString(2, "Martinez"); ps.setString(3, "11111111"); ps.executeUpdate();
            ps.setString(1, "Pedro"); ps.setString(2, "Ramirez"); ps.setString(3, "22222222"); ps.executeUpdate();
            ps.setString(1, "Sofia"); ps.setString(2, "Torres"); ps.setString(3, "33333333"); ps.executeUpdate();
            ps.setString(1, "Juan"); ps.setString(2, "Perez"); ps.setString(3, "12345678"); ps.executeUpdate();
            ps.setString(1, "Maria"); ps.setString(2, "Gonzalez"); ps.setString(3, "23456789"); ps.executeUpdate();
            ps.setString(1, "Carlos"); ps.setString(2, "Lopez"); ps.setString(3, "34567890"); ps.executeUpdate();
            ps.setString(1, "Laura"); ps.setString(2, "Fernandez"); ps.setString(3, "45678901"); ps.executeUpdate();
            ps.setString(1, "Roberto"); ps.setString(2, "Diaz"); ps.setString(3, "56789012"); ps.executeUpdate();
            ps.setString(1, "Admin"); ps.setString(2, "Sistema"); ps.setString(3, "99999999"); ps.executeUpdate();
            ps.setString(1, "Paciente"); ps.setString(2, "Demo"); ps.setString(3, "00000001"); ps.executeUpdate();
        }
    }

    private static void insertarMedicos(Connection conn) throws SQLException {
        String sql = "INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 1); ps.setInt(2, 1); ps.setString(3, "RM12345"); ps.setDouble(4, 50000.0); ps.executeUpdate();
            ps.setInt(1, 2); ps.setInt(2, 2); ps.setString(3, "RM12346"); ps.setDouble(4, 60000.0); ps.executeUpdate();
            ps.setInt(1, 3); ps.setInt(2, 3); ps.setString(3, "RM12347"); ps.setDouble(4, 80000.0); ps.executeUpdate();
        }
    }

    private static void insertarPacientes(Connection conn) throws SQLException {
        String sql = "INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 4); ps.setString(2, "HC001"); ps.setString(3, "Ninguna"); ps.setString(4, "O+"); ps.executeUpdate();
            ps.setInt(1, 5); ps.setString(2, "HC002"); ps.setString(3, "Penicilina"); ps.setString(4, "A+"); ps.executeUpdate();
            ps.setInt(1, 6); ps.setString(2, "HC003"); ps.setString(3, "Ninguna"); ps.setString(4, "B+"); ps.executeUpdate();
            ps.setInt(1, 7); ps.setString(2, "HC004"); ps.setString(3, "Aspirina"); ps.setString(4, "AB+"); ps.executeUpdate();
            ps.setInt(1, 8); ps.setString(2, "HC005"); ps.setString(3, "Ninguna"); ps.setString(4, "O-"); ps.executeUpdate();
            ps.setInt(1, 10); ps.setString(2, "HC006"); ps.setString(3, "Ninguna"); ps.setString(4, "A+"); ps.executeUpdate();
        }
    }

    private static void insertarHorarios(Connection conn) throws SQLException {
        String sql = "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int med = 1; med <= 3; med++) {
                for (int dia = 1; dia <= 5; dia++) {
                    ps.setInt(1, med);
                    ps.setInt(2, dia);
                    ps.setString(3, med == 2 ? "14:00" : (med == 3 ? "09:00" : "08:00"));
                    ps.setString(4, med == 2 ? "18:00" : (med == 3 ? "13:00" : "12:00"));
                    ps.setInt(5, 30);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void insertarUsuarios(Connection conn) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, contrasena_hash, id_persona, rol) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "amedico");
            ps.setString(2, Hasher.hash("medico123"));
            ps.setInt(3, 1);
            ps.setString(4, "MEDICO");
            ps.executeUpdate();

            ps.setString(1, "aadmin");
            ps.setString(2, Hasher.hash("admin123"));
            ps.setInt(3, 9);
            ps.setString(4, "ADMIN");
            ps.executeUpdate();

            ps.setString(1, "apaciente");
            ps.setString(2, Hasher.hash("paciente123"));
            ps.setInt(3, 10);
            ps.setString(4, "PACIENTE");
            ps.executeUpdate();
        }
    }

    private static void insertarCitas(Connection conn) throws SQLException {
        String sql = "INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, creada_por) VALUES (?, ?, ?, ?, ?)";
        LocalDate today = LocalDate.now();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDate[][] fechas = {
                {today.minusDays(1), today.minusDays(1), today.minusDays(1)},
                {today.minusDays(1), today.minusDays(1)},
                {today.plusDays(1), today.plusDays(1)},
                {today.plusDays(2), today.plusDays(2), today.plusDays(2)}
            };
            String[] estados = {"ATENDIDA", "ATENDIDA", "ATENDIDA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA", "PROGRAMADA"};
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
