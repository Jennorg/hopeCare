package com.esperanza.hopecare.modules.citas_consultas.dao;

import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DT_FMT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DT_FMT_ISO_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private LocalDateTime parseFechaHora(ResultSet rs) throws SQLException {
        String raw = rs.getString("fecha_hora");
        if (raw == null) return null;
        try {
            return LocalDateTime.parse(raw, DT_FMT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(raw, DT_FMT_ISO);
            } catch (Exception e2) {
                try {
                    return LocalDateTime.parse(raw, DT_FMT_ISO_MS);
                } catch (Exception e3) {
                    try {
                        long epoch = Long.parseLong(raw);
                        return new java.sql.Timestamp(epoch).toLocalDateTime();
                    } catch (NumberFormatException nfe) {
                        throw new SQLException("Cannot parse fecha_hora: " + raw, e);
                    }
                }
            }
        }
    }

    public List<Cita> obtenerCitasPorMedicoYFecha(int idMedico, LocalDate fecha) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT id_cita, id_paciente, id_medico, fecha_hora, estado, motivo, creada_por, fecha_creacion " +
                     "FROM cita WHERE id_medico = ? AND DATE(fecha_hora) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMedico);
            pstmt.setString(2, fecha.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }
    
    public boolean insertarCita(Cita cita) {
        String sql = "INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, motivo, creada_por, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, cita.getIdPaciente());
            pstmt.setInt(2, cita.getIdMedico());
            
            LocalDateTime fh = cita.getFechaHora();
            pstmt.setString(3, fh != null ? fh.format(DT_FMT) : null);
            
            pstmt.setString(4, cita.getEstado());
            pstmt.setString(5, cita.getMotivo());
            pstmt.setInt(6, cita.getCreadaPor());
            
            LocalDateTime fc = cita.getFechaCreacion();
            if (fc == null) {
                fc = LocalDateTime.now();
                cita.setFechaCreacion(fc);
            }
            pstmt.setTimestamp(7, Timestamp.valueOf(fc));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cita.setIdCita(generatedKeys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean actualizarCita(Cita cita) {
        String sql = "UPDATE cita SET id_medico = ?, fecha_hora = ?, estado = ?, motivo = ?, creada_por = ?, fecha_creacion = ? WHERE id_cita = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cita.getIdMedico());
            
            LocalDateTime fh = cita.getFechaHora();
            pstmt.setString(2, fh != null ? fh.format(DT_FMT) : null);
            
            pstmt.setString(3, cita.getEstado());
            pstmt.setString(4, cita.getMotivo());
            pstmt.setInt(5, cita.getCreadaPor());
            
            LocalDateTime fc = cita.getFechaCreacion();
            if (fc == null) {
                fc = LocalDateTime.now();
                cita.setFechaCreacion(fc);
            }
            pstmt.setTimestamp(6, Timestamp.valueOf(fc));
            
            pstmt.setInt(7, cita.getIdCita());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int idCita, String nuevoEstado) {
        String sql = "UPDATE cita SET estado = ? WHERE id_cita = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idCita);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Cita> obtenerCitasPorEstado(String estado) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT id_cita, id_paciente, id_medico, fecha_hora, estado, motivo, creada_por, fecha_creacion " +
                     "FROM cita WHERE estado = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }

    public List<Cita> listarTodasConNombres() {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_medico, c.fecha_hora, c.estado, c.motivo, c.creada_por, c.fecha_creacion, "
                   + "pp.nombre AS p_nombre, pp.apellido AS p_apellido, pp.documento_identidad AS p_documento, "
                   + "pm.nombre AS m_nombre, pm.apellido AS m_apellido, "
                   + "COALESCE(cs.precio, 0.0) AS precio "
                   + "FROM cita c "
                   + "JOIN paciente pa ON c.id_paciente = pa.id_paciente "
                   + "JOIN persona pp ON pa.id_persona = pp.id_persona "
                   + "JOIN medico me ON c.id_medico = me.id_medico "
                   + "JOIN persona pm ON me.id_persona = pm.id_persona "
                   + "LEFT JOIN consulta cs ON c.id_cita = cs.id_cita "
                   + "ORDER BY c.fecha_hora DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                c.setPacienteNombre(rs.getString("p_nombre") + " " + rs.getString("p_apellido"));
                c.setPacienteDocumento(rs.getString("p_documento"));
                c.setMedicoNombre(rs.getString("m_nombre") + " " + rs.getString("m_apellido"));
                c.setPrecio(rs.getDouble("precio"));
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }

    public boolean eliminarCita(int idCita) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement delConsulta = conn.prepareStatement("DELETE FROM consulta WHERE id_cita = ?")) {
                delConsulta.setInt(1, idCita);
                delConsulta.executeUpdate();
            }

            try (PreparedStatement delCita = conn.prepareStatement("DELETE FROM cita WHERE id_cita = ?")) {
                delCita.setInt(1, idCita);
                if (delCita.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Cita> listarPorMedicoConNombres(int idMedico) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_medico, c.fecha_hora, c.estado, c.motivo, c.creada_por, c.fecha_creacion, "
                   + "pp.nombre AS p_nombre, pp.apellido AS p_apellido, pp.documento_identidad AS p_documento, "
                   + "pm.nombre AS m_nombre, pm.apellido AS m_apellido, "
                   + "COALESCE(cs.precio, 0.0) AS precio "
                   + "FROM cita c "
                   + "JOIN paciente pa ON c.id_paciente = pa.id_paciente "
                   + "JOIN persona pp ON pa.id_persona = pp.id_persona "
                   + "JOIN medico me ON c.id_medico = me.id_medico "
                   + "JOIN persona pm ON me.id_persona = pm.id_persona "
                   + "LEFT JOIN consulta cs ON c.id_cita = cs.id_cita "
                   + "WHERE c.id_medico = ? "
                   + "ORDER BY c.fecha_hora DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMedico);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                c.setPacienteNombre(rs.getString("p_nombre") + " " + rs.getString("p_apellido"));
                c.setPacienteDocumento(rs.getString("p_documento"));
                c.setMedicoNombre(rs.getString("m_nombre") + " " + rs.getString("m_apellido"));
                c.setPrecio(rs.getDouble("precio"));
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }

    public List<Cita> listarPorPacienteConNombres(int idPaciente) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_medico, c.fecha_hora, c.estado, c.motivo, c.creada_por, c.fecha_creacion, "
                   + "pp.nombre AS p_nombre, pp.apellido AS p_apellido, pp.documento_identidad AS p_documento, "
                   + "pm.nombre AS m_nombre, pm.apellido AS m_apellido, "
                   + "COALESCE(cs.precio, 0.0) AS precio "
                   + "FROM cita c "
                   + "JOIN paciente pa ON c.id_paciente = pa.id_paciente "
                   + "JOIN persona pp ON pa.id_persona = pp.id_persona "
                   + "JOIN medico me ON c.id_medico = me.id_medico "
                   + "JOIN persona pm ON me.id_persona = pm.id_persona "
                   + "LEFT JOIN consulta cs ON c.id_cita = cs.id_cita "
                   + "WHERE c.id_paciente = ? "
                   + "ORDER BY c.fecha_hora DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPaciente);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                c.setPacienteNombre(rs.getString("p_nombre") + " " + rs.getString("p_apellido"));
                c.setPacienteDocumento(rs.getString("p_documento"));
                c.setMedicoNombre(rs.getString("m_nombre") + " " + rs.getString("m_apellido"));
                c.setPrecio(rs.getDouble("precio"));
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }

    public List<Cita> listarConsultasAtendidas() {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_medico, c.fecha_hora, c.estado, c.motivo, c.creada_por, c.fecha_creacion, "
                   + "pp.nombre AS p_nombre, pp.apellido AS p_apellido, pp.documento_identidad AS p_documento, "
                   + "pm.nombre AS m_nombre, pm.apellido AS m_apellido, "
                   + "cs.id_consulta, cs.diagnostico, cs.fecha_consulta, cs.precio "
                   + "FROM consulta cs "
                   + "JOIN cita c ON cs.id_cita = c.id_cita "
                   + "JOIN paciente pa ON c.id_paciente = pa.id_paciente "
                   + "JOIN persona pp ON pa.id_persona = pp.id_persona "
                   + "JOIN medico me ON c.id_medico = me.id_medico "
                   + "JOIN persona pm ON me.id_persona = pm.id_persona "
                   + "ORDER BY cs.fecha_consulta DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                c.setPacienteNombre(rs.getString("p_nombre") + " " + rs.getString("p_apellido"));
                c.setPacienteDocumento(rs.getString("p_documento"));
                c.setMedicoNombre(rs.getString("m_nombre") + " " + rs.getString("m_apellido"));
                c.setConsultaId(rs.getInt("id_consulta"));
                c.setConsultaDiagnostico(rs.getString("diagnostico"));
                c.setPrecio(rs.getDouble("precio"));
                String cf = rs.getString("fecha_consulta");
                if (cf != null) {
                    try {
                        c.setConsultaFecha(java.time.LocalDateTime.parse(cf, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } catch (Exception e) {
                        try {
                            c.setConsultaFecha(java.time.LocalDateTime.parse(cf));
                        } catch (Exception ignored) {}
                    }
                }
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }

    public List<Cita> listarConsultasAtendidasPorMedico(int idMedico) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_medico, c.fecha_hora, c.estado, c.motivo, c.creada_por, c.fecha_creacion, "
                   + "pp.nombre AS p_nombre, pp.apellido AS p_apellido, pp.documento_identidad AS p_documento, "
                   + "pm.nombre AS m_nombre, pm.apellido AS m_apellido, "
                   + "cs.id_consulta, cs.diagnostico, cs.fecha_consulta, cs.precio "
                   + "FROM consulta cs "
                   + "JOIN cita c ON cs.id_cita = c.id_cita "
                   + "JOIN paciente pa ON c.id_paciente = pa.id_paciente "
                   + "JOIN persona pp ON pa.id_persona = pp.id_persona "
                   + "JOIN medico me ON c.id_medico = me.id_medico "
                   + "JOIN persona pm ON me.id_persona = pm.id_persona "
                   + "WHERE c.id_medico = ? "
                   + "ORDER BY cs.fecha_consulta DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMedico);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                c.setPacienteNombre(rs.getString("p_nombre") + " " + rs.getString("p_apellido"));
                c.setPacienteDocumento(rs.getString("p_documento"));
                c.setMedicoNombre(rs.getString("m_nombre") + " " + rs.getString("m_apellido"));
                c.setConsultaId(rs.getInt("id_consulta"));
                c.setConsultaDiagnostico(rs.getString("diagnostico"));
                c.setPrecio(rs.getDouble("precio"));
                String cf = rs.getString("fecha_consulta");
                if (cf != null) {
                    try {
                        c.setConsultaFecha(java.time.LocalDateTime.parse(cf, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } catch (Exception e) {
                        try {
                            c.setConsultaFecha(java.time.LocalDateTime.parse(cf));
                        } catch (Exception ignored) {}
                    }
                }
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }

    public List<Cita> obtenerCitasPorEstadoConNombres(String estado) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_medico, c.fecha_hora, c.estado, c.motivo, c.creada_por, c.fecha_creacion, "
                   + "pp.nombre AS p_nombre, pp.apellido AS p_apellido, "
                   + "pm.nombre AS m_nombre, pm.apellido AS m_apellido "
                   + "FROM cita c "
                   + "JOIN paciente pa ON c.id_paciente = pa.id_paciente "
                   + "JOIN persona pp ON pa.id_persona = pp.id_persona "
                   + "JOIN medico me ON c.id_medico = me.id_medico "
                   + "JOIN persona pm ON me.id_persona = pm.id_persona "
                   + "WHERE c.estado = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Cita c = new Cita();
                c.setIdCita(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdMedico(rs.getInt("id_medico"));
                c.setFechaHora(parseFechaHora(rs));
                c.setEstado(rs.getString("estado"));
                c.setMotivo(rs.getString("motivo"));
                c.setCreadaPor(rs.getInt("creada_por"));
                Timestamp ts = rs.getTimestamp("fecha_creacion");
                if (ts != null) {
                    c.setFechaCreacion(ts.toLocalDateTime());
                }
                c.setPacienteNombre(rs.getString("p_nombre") + " " + rs.getString("p_apellido"));
                c.setMedicoNombre(rs.getString("m_nombre") + " " + rs.getString("m_apellido"));
                citas.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return citas;
    }
}
