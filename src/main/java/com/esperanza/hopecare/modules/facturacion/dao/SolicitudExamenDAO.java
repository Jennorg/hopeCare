package com.esperanza.hopecare.modules.facturacion.dao;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.facturacion.dto.PendienteDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SolicitudExamenDAO {
    public List<Object[]> listarNoFacturadasPorPaciente(int idPaciente, Connection conn) throws SQLException {
        String sql = "SELECT s.id_solicitud, e.precio "
                   + "FROM solicitud_examen s "
                   + "JOIN examen_laboratorio e ON s.id_examen = e.id_examen "
                   + "WHERE s.id_paciente = ? AND s.estado = 'COMPLETADO' AND s.facturado = 0";
        List<Object[]> resultados = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultados.add(new Object[]{rs.getInt(1), rs.getDouble(2)});
            }
        }
        return resultados;
    }

    public boolean marcarFacturado(int idReferencia, Connection conn) throws SQLException {
        String sql = "UPDATE solicitud_examen SET facturado = 1 WHERE id_solicitud = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReferencia);
            return ps.executeUpdate() == 1;
        }
    }

    public List<PendienteDTO> listarPendientesConPaciente() {
        List<PendienteDTO> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_paciente, "
                   + "per.nombre || ' ' || per.apellido, "
                   + "e.nombre_examen, e.precio, "
                   + "s.fecha_solicitud "
                   + "FROM solicitud_examen s "
                   + "JOIN paciente p ON s.id_paciente = p.id_paciente "
                   + "JOIN persona per ON p.id_persona = per.id_persona "
                   + "JOIN examen_laboratorio e ON s.id_examen = e.id_examen "
                   + "WHERE s.estado = 'COMPLETADO' AND s.facturado = 0 "
                   + "ORDER BY s.fecha_solicitud DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idSolicitud = rs.getInt(1);
                int idPac = rs.getInt(2);
                String paciente = rs.getString(3);
                String examName = rs.getString(4);
                double precio = rs.getDouble(5);
                String fecha = rs.getString(6);
                lista.add(new PendienteDTO(idPac, idSolicitud, paciente,
                    "Examen: " + examName, precio, fecha, "EXAMEN"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
