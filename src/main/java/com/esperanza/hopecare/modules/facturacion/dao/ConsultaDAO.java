package com.esperanza.hopecare.modules.facturacion.dao;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.facturacion.dto.PendienteDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {
    public List<Object[]> listarNoFacturadasPorPaciente(int idPaciente, Connection conn) throws SQLException {
        String sql = "SELECT c.id_consulta, c.precio "
                   + "FROM consulta c "
                   + "JOIN cita ci ON c.id_cita = ci.id_cita "
                   + "WHERE ci.id_paciente = ? AND ci.estado = 'ATENDIDA' AND c.facturado = 0";
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
        String sql = "UPDATE consulta SET facturado = 1 WHERE id_consulta = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReferencia);
            return ps.executeUpdate() == 1;
        }
    }

    public List<PendienteDTO> listarPendientesConPaciente() {
        List<PendienteDTO> lista = new ArrayList<>();
        String sql = "SELECT c.id_consulta, ci.id_paciente, "
                   + "per.nombre || ' ' || per.apellido, "
                   + "c.precio, ci.fecha_hora "
                   + "FROM consulta c "
                   + "JOIN cita ci ON c.id_cita = ci.id_cita "
                   + "JOIN paciente p ON ci.id_paciente = p.id_paciente "
                   + "JOIN persona per ON p.id_persona = per.id_persona "
                   + "WHERE ci.estado = 'ATENDIDA' AND c.facturado = 0 "
                   + "ORDER BY ci.fecha_hora DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idConsulta = rs.getInt(1);
                int idPac = rs.getInt(2);
                String paciente = rs.getString(3);
                double precio = rs.getDouble(4);
                String fecha = rs.getString(5);
                lista.add(new PendienteDTO(idPac, idConsulta, paciente,
                    "Consulta medica #" + idConsulta, precio, fecha, "CONSULTA"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
