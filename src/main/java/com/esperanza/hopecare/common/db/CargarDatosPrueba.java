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
                insertarRol(conn, "RECEPCION");
                insertarRol(conn, "FARMACIA");
                insertarRol(conn, "LABORATORIO");

                insertarEspecialidad(conn, "Medicina General");
                insertarEspecialidad(conn, "Pediatría");
                insertarEspecialidad(conn, "Traumatología");
                insertarEspecialidad(conn, "Cardiología");
                insertarEspecialidad(conn, "Dermatología");

                insertarMedicamento(conn, "Paracetamol", "Paracetamol", "Tabletas", "500mg", 100.0, 100, 20, true);
                insertarMedicamento(conn, "Ibuprofeno", "Ibuprofeno", "Tabletas", "400mg", 50.0, 50, 15, true);
                insertarMedicamento(conn, "Amoxicilina", "Amoxicilina", "Cápsulas", "500mg", 30.0, 30, 10, true);
                insertarMedicamento(conn, "Losartán", "Losartán Potásico", "Tabletas", "50mg", 40.0, 40, 5, true);
                insertarMedicamento(conn, "Omeprazol", "Omeprazol", "Cápsulas", "20mg", 60.0, 60, 10, true);

                int pAdmin = insertarPersona(conn, "Admin", "Sistema", "99999999", null, null, "admin@hopecare.com", null, null);
                int pRecep = insertarPersona(conn, "Recepcion", "Sistema", "99999998", null, null, "recepcion@hopecare.com", null, null);
                int pFarma = insertarPersona(conn, "Farmacia", "Sistema", "99999997", null, null, "farmacia@hopecare.com", null, null);
                int pLab = insertarPersona(conn, "Laboratorio", "Sistema", "99999996", null, null, "laboratorio@hopecare.com", null, null);

                int u1 = insertarUsuario(conn, "admin", "admin123", 1, pAdmin);
                int u2 = insertarUsuario(conn, "recepcion", "recepcion123", 2, pRecep);
                int u3 = insertarUsuario(conn, "farmacia", "farmacia123", 3, pFarma);
                int u4 = insertarUsuario(conn, "laboratorio", "laboratorio123", 4, pLab);

                int pp1 = insertarPersona(conn, "Juan", "Pérez", "12345678", "1980-01-15", "123456789", "juan.perez@email.com", "Calle 123 #45-67", "M");
                insertarPaciente(conn, pp1, "HC001", "Ninguna", "O+", "María Pérez - 987654321");
                int pp2 = insertarPersona(conn, "María", "González", "23456789", "1985-05-20", "234567890", "maria.gonzalez@email.com", "Calle 456 #78-90", "F");
                insertarPaciente(conn, pp2, "HC002", "Penicilina", "A+", "Roberto González - 876543210");
                int pp3 = insertarPersona(conn, "Carlos", "López", "34567890", "1990-08-10", "345678901", "carlos.lopez@email.com", "Carrera 789 #12-34", "M");
                insertarPaciente(conn, pp3, "HC003", "Ninguna", "B+", "Laura López - 765432109");
                int pp4 = insertarPersona(conn, "Laura", "Fernández", "45678901", "1975-12-03", "456789012", "laura.fernandez@email.com", "Avenida 123 #45-67", "F");
                insertarPaciente(conn, pp4, "HC004", "Aspirina", "AB+", "Carlos Fernández - 654321098");
                int pp5 = insertarPersona(conn, "Roberto", "Díaz", "56789012", "1988-03-25", "567890123", "roberto.diaz@email.com", "Diagonal 456 #78-90", "M");
                insertarPaciente(conn, pp5, "HC005", "Ninguna", "O-", "Laura Díaz - 543210987");

                int pp6 = insertarPersona(conn, "Luis", "Mendoza", "67890123", "1992-06-15", "678901234", "luis.mendoza@email.com", "Calle 111 #22-33", "M");
                insertarPaciente(conn, pp6, "HC006", "Ninguna", "A+", "María Mendoza - 111111111");
                int pp7 = insertarPersona(conn, "Ana", "Castillo", "78901234", "1983-11-08", "789012345", "ana.castillo@email.com", "Carrera 222 #33-44", "F");
                insertarPaciente(conn, pp7, "HC007", "Sulfa", "O+", "Pedro Castillo - 222222222");
                int pp8 = insertarPersona(conn, "Diego", "Rojas", "89012345", "1995-02-28", "890123456", "diego.rojas@email.com", "Avenida 333 #44-55", "M");
                insertarPaciente(conn, pp8, "HC008", "Ninguna", "B-", "Laura Rojas - 333333333");

                int pm1 = insertarPersona(conn, "Ana", "Martínez", "87654321", "1970-07-15", "678901234", "ana.martinez@email.com", "Calle 789 #12-34", "F");
                insertarMedico(conn, pm1, 1, "RM12345", 50000.0);
                int pm2 = insertarPersona(conn, "Pedro", "Ramírez", "98765432", "1972-11-22", "789012345", "pedro.ramirez@email.com", "Carrera 123 #45-67", "M");
                insertarMedico(conn, pm2, 2, "RM12346", 60000.0);
                int pm3 = insertarPersona(conn, "Sofía", "Torres", "11111111", "1980-04-30", "890123456", "sofia.torres@email.com", "Avenida 456 #78-90", "F");
                insertarMedico(conn, pm3, 4, "RM12347", 80000.0);

                int pm4 = insertarPersona(conn, "Carmen", "Vega", "22222222", "1978-09-12", "901234567", "carmen.vega@email.com", "Calle 444 #55-66", "F");
                insertarMedico(conn, pm4, 3, "RM12348", 55000.0);
                int pm5 = insertarPersona(conn, "Jorge", "Herrera", "33333333", "1975-03-20", "012345678", "jorge.herrera@email.com", "Carrera 555 #66-77", "M");
                insertarMedico(conn, pm5, 5, "RM12349", 70000.0);

                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
                LocalDateTime dayAfter = LocalDateTime.now().plusDays(2);
                LocalDateTime now = LocalDateTime.now();

                // Citas: primeras 3 ATENDIDA (con consulta), resto PROGRAMADA
                insertarCita(conn, 1, 1, yesterday.withHour(9).withMinute(0), "ATENDIDA", "Consulta de rutina", 1, yesterday);
                insertarCita(conn, 2, 1, yesterday.withHour(9).withMinute(30), "ATENDIDA", "Revisión general", 1, yesterday);
                insertarCita(conn, 3, 1, yesterday.withHour(10).withMinute(0), "ATENDIDA", "Chequeo anual", 1, yesterday);
                insertarCita(conn, 4, 2, yesterday.withHour(14).withMinute(0), "PROGRAMADA", "Consulta pediátrica", 2, yesterday);
                insertarCita(conn, 5, 2, yesterday.withHour(14).withMinute(30), "PROGRAMADA", "Seguimiento", 2, yesterday);
                insertarCita(conn, 1, 3, tomorrow.withHour(9).withMinute(0), "PROGRAMADA", "Consulta cardiológica", 3, now);
                insertarCita(conn, 2, 3, tomorrow.withHour(10).withMinute(0), "PROGRAMADA", "Control", 3, now);
                insertarCita(conn, 3, 2, dayAfter.withHour(14).withMinute(0), "PROGRAMADA", "Consulta pediátrica", 2, now);
                insertarCita(conn, 4, 2, dayAfter.withHour(15).withMinute(0), "PROGRAMADA", "Seguimiento", 2, now);
                insertarCita(conn, 5, 3, dayAfter.withHour(9).withMinute(30), "PROGRAMADA", "Consulta cardiológica", 3, now);

                LocalDateTime hoy = LocalDateTime.now();

                int idCitaAtendidaHoy = insertarCita(conn, 6, 4, hoy.withHour(9).withMinute(0), "ATENDIDA", "Dolor lumbar - traumatología", 1, hoy);
                insertarCita(conn, 7, 5, hoy.withHour(10).withMinute(0), "PROGRAMADA", "Control dermatológico", 1, hoy);
                insertarCita(conn, 8, 4, hoy.withHour(11).withMinute(0), "CANCELADA", "Revisión de fractura", 1, hoy);
                insertarCita(conn, 6, 1, hoy.withHour(15).withMinute(0), "CANCELADA", "Medicina general", 1, hoy);
                insertarCita(conn, 7, 2, hoy.withHour(8).withMinute(0), "NO_ASISTIO", "Pediatría control", 1, hoy);
                insertarCita(conn, 8, 5, hoy.withHour(14).withMinute(0), "NO_ASISTIO", "Consulta dermatológica", 1, hoy);
                insertarCita(conn, 6, 3, hoy.withHour(16).withMinute(0), "NO_ASISTIO", "Cardiología seguimiento", 1, hoy);

                // Consultas: 2 pendientes (facturado=false), 1 facturada (true)
                insertarConsulta(conn, 1, "Paciente presenta síntomas de gripe", "Fiebre, tos, dolor de garganta", "Reposo y paracetamol", "", yesterday, false, 50000.0);
                insertarConsulta(conn, 2, "Revisión general sin novedades", "Ninguno", "Paciente sano", "", yesterday, false, 45000.0);
                insertarConsulta(conn, 3, "Control anual normal", "Ninguno", "Continuar con hábitos saludables", "", yesterday, true, 80000.0);
                insertarConsulta(conn, idCitaAtendidaHoy, "Paciente con dolor lumbar crónico", "Dolor lumbar, irradiación a pierna izquierda", "Antiinflamatorios, fisioterapia y reposo 48h", "", hoy, false, 55000.0);

                // Facturas existentes
                int f1 = insertarFactura(conn, 3, 80090.0, 15217.1, 95307.1, "PAGADO");
                insertarDetalleFactura(conn, f1, "Consulta médica #3", 3, "CONSULTA", 80000.0);
                insertarDetalleFactura(conn, f1, "Medicamento: Amoxicilina", 3, "MEDICAMENTO", 90.0);
                int f2 = insertarFactura(conn, 3, 12000.0, 2280.0, 14280.0, "PENDIENTE");
                insertarDetalleFactura(conn, f2, "Examen: Colesterol total", 3, "EXAMEN", 12000.0);

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
        String sql = "INSERT OR IGNORE INTO rol (nombre_rol) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
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

    private static int insertarUsuario(Connection conn, String nombreUsuario, String password, int idRol, int idPersona) throws SQLException {
        String sql = "INSERT OR IGNORE INTO usuario (nombre_usuario, contrasena_hash, id_rol, id_persona) VALUES (?, ?, ?, ?)";
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

    private static void insertarMedicamento(Connection conn, String nombreComercial, String principioActivo, String presentacion, String concentracion, double precioUnitario, int stockActual, int stockMinimo, boolean requiereReceta) throws SQLException {
        String sql = "INSERT OR IGNORE INTO medicamento (nombre_comercial, principio_activo, presentacion, concentracion, precio_unitario, stock_actual, stock_minimo, requiere_receta) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreComercial);
            ps.setString(2, principioActivo);
            ps.setString(3, presentacion);
            ps.setString(4, concentracion);
            ps.setDouble(5, precioUnitario);
            ps.setInt(6, stockActual);
            ps.setInt(7, stockMinimo);
            ps.setBoolean(8, requiereReceta);
            ps.executeUpdate();
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

    private static int insertarCita(Connection conn, int idPaciente, int idMedico, LocalDateTime fechaHora, String estado, String motivo, int creadaPor, LocalDateTime fechaCreacion) throws SQLException {
        String sql = "INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, motivo, creada_por, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPaciente);
            ps.setInt(2, idMedico);
            ps.setString(3, fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setString(4, estado);
            ps.setString(5, motivo);
            ps.setInt(6, creadaPor);
            ps.setString(7, fechaCreacion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener id_cita");
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

    private static void marcarFacturadoConsulta(Connection conn, int idCita) throws SQLException {
        String sql = "UPDATE consulta SET facturado = 1 WHERE id_cita = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            ps.executeUpdate();
        }
    }
}