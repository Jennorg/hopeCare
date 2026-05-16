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
                insertarRol(conn, "ADMIN", "Administrador del sistema");
                insertarRol(conn, "RECEPCION", "Personal de recepción");
                insertarRol(conn, "FARMACIA", "Personal de farmacia");
                insertarRol(conn, "LABORATORIO", "Personal de laboratorio");

                insertarEspecialidad(conn, "Medicina General", "Especialidad en medicina general");
                insertarEspecialidad(conn, "Pediatría", "Especialidad en pediatría");
                insertarEspecialidad(conn, "Traumatología", "Especialidad en traumatología");
                insertarEspecialidad(conn, "Cardiología", "Especialidad en cardiología");
                insertarEspecialidad(conn, "Dermatología", "Especialidad en dermatología");

                insertarMedicamento(conn, "Paracetamol", "Paracetamol", "Tabletas", "500mg", 100.0, 100, 20, true);
                insertarMedicamento(conn, "Ibuprofeno", "Ibuprofeno", "Tabletas", "400mg", 50.0, 50, 15, true);
                insertarMedicamento(conn, "Amoxicilina", "Amoxicilina", "Cápsulas", "500mg", 30.0, 30, 10, true);
                insertarMedicamento(conn, "Losartán", "Losartán Potásico", "Tabletas", "50mg", 40.0, 40, 5, true);
                insertarMedicamento(conn, "Omeprazol", "Omeprazol", "Cápsulas", "20mg", 60.0, 60, 10, true);

                insertarExamenLab(conn, "Hemograma completo", "Análisis de sangre completo", 25000.0, 4, null);
                insertarExamenLab(conn, "Glucosa", "Medición de glucosa en sangre", 8000.0, 2, null);
                insertarExamenLab(conn, "Colesterol total", "Perfil lipídico", 12000.0, 3, null);
                insertarExamenLab(conn, "Radiografía de tórax", "Rayos X de tórax", 35000.0, 24, null);
                insertarExamenLab(conn, "Electrocardiograma", "ECG en reposo", 20000.0, 6, null);

                int u1 = insertarUsuario(conn, "admin", "admin123", 1);
                int u2 = insertarUsuario(conn, "recepcion", "recepcion123", 2);
                int u3 = insertarUsuario(conn, "farmacia", "farmacia123", 3);
                int u4 = insertarUsuario(conn, "laboratorio", "laboratorio123", 4);

                int pp1 = insertarPersona(conn, "PACIENTE", "Juan", "Pérez", "12345678", "1980-01-15", "123456789", "juan.perez@email.com", "Calle 123 #45-67", "M", u1);
                insertarPaciente(conn, pp1, "HC001", "Ninguna", "O+", "María Pérez - 987654321");
                int pp2 = insertarPersona(conn, "PACIENTE", "María", "González", "23456789", "1985-05-20", "234567890", "maria.gonzalez@email.com", "Calle 456 #78-90", "F", u2);
                insertarPaciente(conn, pp2, "HC002", "Penicilina", "A+", "Roberto González - 876543210");
                int pp3 = insertarPersona(conn, "PACIENTE", "Carlos", "López", "34567890", "1990-08-10", "345678901", "carlos.lopez@email.com", "Carrera 789 #12-34", "M", u3);
                insertarPaciente(conn, pp3, "HC003", "Ninguna", "B+", "Laura López - 765432109");
                int pp4 = insertarPersona(conn, "PACIENTE", "Laura", "Fernández", "45678901", "1975-12-03", "456789012", "laura.fernandez@email.com", "Avenida 123 #45-67", "F", u4);
                insertarPaciente(conn, pp4, "HC004", "Aspirina", "AB+", "Carlos Fernández - 654321098");
                int pp5 = insertarPersona(conn, "PACIENTE", "Roberto", "Díaz", "56789012", "1988-03-25", "567890123", "roberto.diaz@email.com", "Diagonal 456 #78-90", "M", u1);
                insertarPaciente(conn, pp5, "HC005", "Ninguna", "O-", "Laura Díaz - 543210987");

                int pm1 = insertarPersona(conn, "MEDICO", "Ana", "Martínez", "87654321", "1970-07-15", "678901234", "ana.martinez@email.com", "Calle 789 #12-34", "F", u1);
                insertarMedico(conn, pm1, 1, "RM12345");
                int pm2 = insertarPersona(conn, "MEDICO", "Pedro", "Ramírez", "98765432", "1972-11-22", "789012345", "pedro.ramirez@email.com", "Carrera 123 #45-67", "M", u2);
                insertarMedico(conn, pm2, 2, "RM12346");
                int pm3 = insertarPersona(conn, "MEDICO", "Sofía", "Torres", "11111111", "1980-04-30", "890123456", "sofia.torres@email.com", "Avenida 456 #78-90", "F", u3);
                insertarMedico(conn, pm3, 4, "RM12347");

                insertarHorario(conn, 1, 1, "08:00", "12:00", 30, true);
                insertarHorario(conn, 1, 2, "08:00", "12:00", 30, true);
                insertarHorario(conn, 1, 3, "08:00", "12:00", 30, true);
                insertarHorario(conn, 1, 4, "08:00", "12:00", 30, true);
                insertarHorario(conn, 1, 5, "08:00", "12:00", 30, true);
                insertarHorario(conn, 2, 1, "14:00", "18:00", 30, true);
                insertarHorario(conn, 2, 2, "14:00", "18:00", 30, true);
                insertarHorario(conn, 2, 3, "14:00", "18:00", 30, true);
                insertarHorario(conn, 2, 4, "14:00", "18:00", 30, true);
                insertarHorario(conn, 3, 1, "09:00", "13:00", 30, true);
                insertarHorario(conn, 3, 2, "09:00", "13:00", 30, true);
                insertarHorario(conn, 3, 3, "09:00", "13:00", 30, true);
                insertarHorario(conn, 3, 4, "09:00", "13:00", 30, true);
                insertarHorario(conn, 3, 5, "09:00", "13:00", 30, true);

                LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
                LocalDateTime dayAfter = LocalDateTime.now().plusDays(2);
                LocalDateTime now = LocalDateTime.now();

                insertarCita(conn, 1, 1, tomorrow.withHour(9).withMinute(0), "PROGRAMADA", "Consulta de rutina", 1, now);
                insertarCita(conn, 2, 1, tomorrow.withHour(9).withMinute(30), "PROGRAMADA", "Revisión general", 1, now);
                insertarCita(conn, 3, 1, tomorrow.withHour(10).withMinute(0), "PROGRAMADA", "Chequeo anual", 1, now);
                insertarCita(conn, 4, 2, tomorrow.withHour(14).withMinute(0), "PROGRAMADA", "Consulta pediátrica", 2, now);
                insertarCita(conn, 5, 2, tomorrow.withHour(14).withMinute(30), "PROGRAMADA", "Seguimiento", 2, now);
                insertarCita(conn, 1, 3, tomorrow.withHour(9).withMinute(0), "PROGRAMADA", "Consulta cardiológica", 3, now);
                insertarCita(conn, 2, 3, tomorrow.withHour(10).withMinute(0), "PROGRAMADA", "Control", 3, now);
                insertarCita(conn, 3, 2, dayAfter.withHour(14).withMinute(0), "PROGRAMADA", "Consulta pediátrica", 2, now);
                insertarCita(conn, 4, 2, dayAfter.withHour(15).withMinute(0), "PROGRAMADA", "Seguimiento", 2, now);
                insertarCita(conn, 5, 3, dayAfter.withHour(9).withMinute(30), "PROGRAMADA", "Consulta cardiológica", 3, now);

                insertarConsulta(conn, 1, "Paciente presenta síntomas de gripe", "Fiebre, tos, dolor de garganta", "Reposo y paracetamol", "", now, false, 50000.0);
                insertarConsulta(conn, 3, "Control anual normal", "Ninguno", "Continuar con hábitos saludables", "", now, false, 80000.0);
                insertarConsulta(conn, 5, "Seguimiento pediátrico", "Revisión de crecimiento", "Desarrollo normal", "", now, false, 60000.0);

                insertarEntregaMedicamento(conn, 1, 1, 2, true, now, 3, false);
                insertarEntregaMedicamento(conn, 2, 2, 1, false, now, 3, false);

                insertarSolicitudExamen(conn, 1, 1, "PENDIENTE", null, null, false);
                insertarSolicitudExamen(conn, 2, 2, "COMPLETADO", "Glucosa: 95 mg/dL (Normal)", null, true);

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

    private static void insertarRol(Connection conn, String nombre, String descripcion) throws SQLException {
        String sql = "INSERT OR IGNORE INTO rol (nombre_rol, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.executeUpdate();
        }
    }

    private static void insertarEspecialidad(Connection conn, String nombre, String descripcion) throws SQLException {
        String sql = "INSERT OR IGNORE INTO especialidad (nombre_especialidad, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.executeUpdate();
        }
    }

    private static int insertarUsuario(Connection conn, String nombreUsuario, String password, int idRol) throws SQLException {
        String sql = "INSERT OR IGNORE INTO usuario (nombre_usuario, contrasena_hash, id_rol, activo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, Hasher.hash(password));
            ps.setInt(3, idRol);
            ps.setInt(4, 1);
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

    private static void insertarExamenLab(Connection conn, String nombre, String descripcion, double precio, int tiempoHoras, byte[] resultadoArchivo) throws SQLException {
        String sql = "INSERT OR IGNORE INTO examen_laboratorio (nombre_examen, descripcion, precio, tiempo_resultado_horas, resultado_archivo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setInt(4, tiempoHoras);
            ps.setBytes(5, resultadoArchivo);
            ps.executeUpdate();
        }
    }

    private static int insertarPersona(Connection conn, String tipo, String nombre, String apellido, String documento,
                                  String fechaNacimiento, String telefono, String email, String direccion, String genero, int idUsuario) throws SQLException {
        String sql = "INSERT INTO persona (tipo_persona, nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tipo);
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            ps.setString(4, documento);
            ps.setString(5, fechaNacimiento);
            ps.setString(6, telefono);
            ps.setString(7, email);
            ps.setString(8, direccion);
            ps.setString(9, genero);
            ps.setInt(10, idUsuario);
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

    private static void insertarMedico(Connection conn, int idPersona, int idEspecialidad, String registroMedico) throws SQLException {
        String sql = "INSERT INTO medico (id_persona, id_especialidad, registro_medico, activo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ps.setInt(2, idEspecialidad);
            ps.setString(3, registroMedico);
            ps.setInt(4, 1);
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

    private static void insertarEntregaMedicamento(Connection conn, int idPaciente, int idMedicamento, int cantidadEntregada, boolean presenteReceta, LocalDateTime fechaEntrega, int entregadoPor, boolean facturado) throws SQLException {
        String sql = "INSERT INTO entrega_medicamento (id_paciente, id_medicamento, cantidad_entregada, presente_receta, fecha_entrega, entregado_por, facturado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ps.setInt(2, idMedicamento);
            ps.setInt(3, cantidadEntregada);
            ps.setBoolean(4, presenteReceta);
            ps.setString(5, fechaEntrega.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setInt(6, entregadoPor);
            ps.setBoolean(7, facturado);
            ps.executeUpdate();
        }
    }

    private static void insertarSolicitudExamen(Connection conn, int idPaciente, int idExamen, String estado, String resultadoTexto, byte[] resultadoArchivo, boolean facturado) throws SQLException {
        String sql = "INSERT INTO solicitud_examen (id_paciente, id_examen, estado, resultado_texto, resultado_archivo, facturado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ps.setInt(2, idExamen);
            ps.setString(3, estado);
            ps.setString(4, resultadoTexto);
            ps.setBytes(5, resultadoArchivo);
            ps.setBoolean(6, facturado);
            ps.executeUpdate();
        }
    }
}