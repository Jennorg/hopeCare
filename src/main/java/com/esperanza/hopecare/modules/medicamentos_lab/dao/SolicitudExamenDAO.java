package com.esperanza.hopecare.modules.medicamentos_lab.dao;

import com.esperanza.hopecare.modules.medicamentos_lab.model.SolicitudExamen;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitudExamenDAO {

    public SolicitudExamen obtenerPorId(int idSolicitud, Connection conn) throws SQLException {
        String sql = "SELECT s.id_solicitud, s.id_paciente, s.id_examen, s.fecha_solicitud, s.estado, s.resultado_texto, s.resultado_archivo, s.realizado_por, s.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, e.nombre_examen " +
                     "FROM solicitud_examen s " +
                     "JOIN paciente pc ON s.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN examen_laboratorio e ON s.id_examen = e.id_examen " +
                     "WHERE s.id_solicitud = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    public List<SolicitudExamen> listarPendientes() {
        List<SolicitudExamen> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_paciente, s.id_examen, s.fecha_solicitud, s.estado, s.resultado_texto, s.resultado_archivo, s.realizado_por, s.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, e.nombre_examen " +
                     "FROM solicitud_examen s " +
                     "JOIN paciente pc ON s.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN examen_laboratorio e ON s.id_examen = e.id_examen " +
                     "WHERE s.estado = 'PENDIENTE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<SolicitudExamen> listarPorPaciente(int idPaciente) {
        List<SolicitudExamen> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_paciente, s.id_examen, s.fecha_solicitud, s.estado, s.resultado_texto, s.resultado_archivo, s.realizado_por, s.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, e.nombre_examen " +
                     "FROM solicitud_examen s " +
                     "JOIN paciente pc ON s.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN examen_laboratorio e ON s.id_examen = e.id_examen " +
                     "WHERE s.id_paciente = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean insertar(SolicitudExamen solicitud, Connection conn) throws SQLException {
        String sql = "INSERT INTO solicitud_examen (id_paciente, id_examen, estado, facturado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, solicitud.getIdPaciente());
            ps.setInt(2, solicitud.getIdExamen());
            ps.setString(3, solicitud.getEstado());
            ps.setBoolean(4, solicitud.isFacturado());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) solicitud.setIdSolicitud(rs.getInt(1));
            }
            return affected > 0;
        }
    }

    public boolean actualizarResultado(int idSolicitud, String resultado, String estado, Connection conn) throws SQLException {
        String sql = "UPDATE solicitud_examen SET resultado_texto = ?, estado = ? WHERE id_solicitud = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resultado);
            ps.setString(2, estado);
            ps.setInt(3, idSolicitud);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean marcarFacturado(int idSolicitud, Connection conn) throws SQLException {
        String sql = "UPDATE solicitud_examen SET facturado = 1 WHERE id_solicitud = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean cancelar(int idSolicitud, Connection conn) throws SQLException {
        String sql = "UPDATE solicitud_examen SET estado = 'CANCELADO' WHERE id_solicitud = ? AND estado = 'PENDIENTE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            return ps.executeUpdate() == 1;
        }
    }

    public List<SolicitudExamen> listarTodas() {
        List<SolicitudExamen> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_paciente, s.id_examen, s.fecha_solicitud, s.estado, s.resultado_texto, s.resultado_archivo, s.realizado_por, s.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, e.nombre_examen " +
                     "FROM solicitud_examen s " +
                     "JOIN paciente pc ON s.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN examen_laboratorio e ON s.id_examen = e.id_examen " +
                     "ORDER BY s.fecha_solicitud DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<SolicitudExamen> listarPorEstado(String estado) {
        List<SolicitudExamen> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_paciente, s.id_examen, s.fecha_solicitud, s.estado, s.resultado_texto, s.resultado_archivo, s.realizado_por, s.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, e.nombre_examen " +
                     "FROM solicitud_examen s " +
                     "JOIN paciente pc ON s.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN examen_laboratorio e ON s.id_examen = e.id_examen " +
                     "WHERE s.estado = ? ORDER BY s.fecha_solicitud DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private SolicitudExamen mapear(ResultSet rs) throws SQLException {
        SolicitudExamen s = new SolicitudExamen();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdPaciente(rs.getInt("id_paciente"));
        s.setIdExamen(rs.getInt("id_examen"));
        Timestamp ts = rs.getTimestamp("fecha_solicitud");
        if (ts != null) s.setFechaSolicitud(ts.toLocalDateTime());
        s.setEstado(rs.getString("estado"));
        s.setResultadoTexto(rs.getString("resultado_texto"));
        s.setResultadoArchivo(rs.getBytes("resultado_archivo"));
        s.setRealizadoPor(rs.getInt("realizado_por"));
        s.setFacturado(rs.getBoolean("facturado"));
        s.setPacienteNombre(rs.getString("paciente_nombre"));
        s.setPacienteApellido(rs.getString("paciente_apellido"));
        s.setExamenNombre(rs.getString("nombre_examen"));
        return s;
    }
}