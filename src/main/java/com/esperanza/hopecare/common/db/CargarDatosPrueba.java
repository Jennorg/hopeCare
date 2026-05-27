package com.esperanza.hopecare.common.db;

import com.esperanza.hopecare.common.utils.Hasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CargarDatosPrueba {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                insertarRol(conn, "ADMIN");
                insertarRol(conn, "SECRETARIA");
                insertarRol(conn, "PACIENTE");

                insertarEspecialidad(conn, "Medicina General");
                insertarEspecialidad(conn, "Pediatría");

                int pAdmin = insertarPersona(conn, "Admin", "Sistema", "99999999", null, null, "admin@hopecare.com", null, null);
                int pSecre = insertarPersona(conn, "Secretaria", "Sistema", "99999998", null, null, "secretaria@hopecare.com", null, null);

                int u1 = insertarUsuario(conn, "admin", "admin123", 1, pAdmin);
                int u2 = insertarUsuario(conn, "secretaria", "secretaria123", 2, pSecre);

                int pp1 = insertarPersona(conn, "Juan", "Pérez", "12345678", "1980-01-15", "123456789", "juan.perez@email.com", "Calle 123 #45-67", "M");
                insertarPaciente(conn, pp1, "HC001", "Ninguna", "O+", "María Pérez - 987654321");
                insertarUsuario(conn, "juan", "juan123", 3, pp1); // Juan is Paciente

                int pp2 = insertarPersona(conn, "María", "González", "23456789", "1985-05-20", "234567890", "maria.gonzalez@email.com", "Calle 456 #78-90", "F");
                insertarPaciente(conn, pp2, "HC002", "Penicilina", "A+", "Roberto González - 876543210");
                insertarUsuario(conn, "maria", "maria123", 3, pp2); // Maria is Paciente

                int pp3 = insertarPersona(conn, "Carlos", "López", "34567890", "1990-08-10", "345678901", "carlos.lopez@email.com", "Carrera 789 #12-34", "M");
                insertarPaciente(conn, pp3, "HC003", "Ninguna", "B+", "Laura López - 765432109");

                int pm1 = insertarPersona(conn, "Ana", "Martínez", "87654321", "1970-07-15", "678901234", "ana.martinez@email.com", "Calle 789 #12-34", "F");
                insertarMedico(conn, pm1, 1, "RM12345", 50000.0);

                insertarHorario(conn, 1, 1, "08:00", "12:00", 30, true);
                insertarHorario(conn, 1, 2, "08:00", "12:00", 30, true);
                insertarHorario(conn, 1, 3, "08:00", "12:00", 30, true);

                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
                LocalDateTime now = LocalDateTime.now();

                // Citas: primeras 3 ATENDIDA (con consulta)
                insertarCita(conn, 1, 1, yesterday.withHour(9).withMinute(0), "ATENDIDA", "Consulta de rutina", 1, yesterday);
                insertarCita(conn, 2, 1, yesterday.withHour(9).withMinute(30), "ATENDIDA", "Revisión general", 1, yesterday);
                insertarCita(conn, 3, 1, yesterday.withHour(10).withMinute(0), "ATENDIDA", "Chequeo anual", 1, yesterday);
                insertarCita(conn, 1, 1, tomorrow.withHour(9).withMinute(0), "PROGRAMADA", "Consulta de control", 1, now);

                // Consultas: 2 pendientes (facturado=false), 1 facturada (true)
                insertarConsulta(conn, 1, "Paciente presenta síntomas de gripe", "Fiebre, tos, dolor de garganta", "Reposo y paracetamol", "", yesterday, false, 50000.0);
                insertarConsulta(conn, 2, "Revisión general sin novedades", "Ninguno", "Paciente sano", "", yesterday, false, 45000.0);
                insertarConsulta(conn, 3, "Control anual normal", "Ninguno", "Continuar con hábitos saludables", "", yesterday, true, 80000.0);

                // Facturas existentes
                int f1 = insertarFactura(conn, 3, 80000.0, 15200.0, 95200.0, "PAGADO");
                insertarDetalleFactura(conn, f1, "Consulta médica #3", 3, "CONSULTA", 80000.0);

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

    private static void insertarEspecialidad(Connection conn, String nombre) throws SQLException {
        String sql = "INSERT IGNORE INTO especialidad (nombre_especialidad) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        }
    }

    private static int insertarUsuario(Connection conn, String nombreUsuario, String password, int idRol, int idPersona) throws SQLException {
        String sql = "INSERT IGNORE INTO usuario (nombre_usuario, contrasena_hash, id_rol, id_persona) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, Hasher.hash(password));
            ps.setInt(3, idRol);
            ps.setInt(4, idPersona);
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            String select = "SELECT id_usuario FROM usuario WHERE nombre_usuario = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(select)) {
                ps2.setString(1, nombreUsuario);
                var rs2 = ps2.executeQuery();
                if (rs2.next()) return rs2.getInt(1);
            }
            throw new SQLException("No se pudo obtener id_usuario");
        }
    }

    private static int insertarPersona(Connection conn, String nombre, String apellido, String documento,
                                  String fechaNacimiento, String telefono, String email, String direccion, String genero) throws SQLException {
        String sql = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
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

    private static void insertarHorario(Connection conn, int idMedico, int diaSemana, String horaInicio, String horaFin, int intervalo, boolean activo) throws SQLException {
        String sql = "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setInt(2, diaSemana);
            ps.setString(3, horaInicio);
            ps.setString(4, horaFin);
            ps.setInt(5, intervalo);
            ps.setBoolean(6, activo);
            ps.executeUpdate();
        }
    }

    private static void insertarCita(Connection conn, int idPaciente, int idMedico, LocalDateTime fechaHora, String estado, String motivo, int creadaPor, LocalDateTime fechaCreacion) throws SQLException {
        String sql = "INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, motivo, creada_por, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ps.setInt(2, idMedico);
            ps.setString(3, fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setString(4, estado);
            ps.setString(5, motivo);
            ps.setInt(6, creadaPor);
            ps.setString(7, fechaCreacion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        }
    }

    private static void insertarConsulta(Connection conn, int idCita, String diagnostico, String sintomas, String tratamiento, String notasMedicas, LocalDateTime fechaConsulta, boolean facturado, double precio) throws SQLException {
        String sql = "INSERT INTO consulta (id_cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta, facturado, precio) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            ps.setString(2, diagnostico);
            ps.setString(3, sintomas);
            ps.setString(4, tratamiento);
            ps.setString(5, notasMedicas);
            ps.setString(6, fechaConsulta.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setBoolean(7, facturado);
            ps.setDouble(8, precio);
            ps.executeUpdate();
        }
    }

    private static int insertarFactura(Connection conn, int idPaciente, double subtotal, double impuesto, double total, String estadoPago) throws SQLException {
        String sql = "INSERT INTO factura (id_paciente, fecha_emision, subtotal, impuesto, total, estado_pago, forma_pago) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPaciente);
            ps.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setDouble(3, subtotal);
            ps.setDouble(4, impuesto);
            ps.setDouble(5, total);
            ps.setString(6, estadoPago);
            ps.setString(7, "EFECTIVO");
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener id_factura");
        }
    }

    private static void insertarDetalleFactura(Connection conn, int idFactura, String concepto, int idReferencia, String tipoReferencia, double monto) throws SQLException {
        String sql = "INSERT INTO detalle_factura (id_factura, concepto, id_referencia, tipo_referencia, monto) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            ps.setString(2, concepto);
            ps.setInt(3, idReferencia);
            ps.setString(4, tipoReferencia);
            ps.setDouble(5, monto);
            ps.executeUpdate();
        }
    }
}
