package com.esperanza.hopecare.modules.facturacion.dao;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.facturacion.dto.PendienteDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntregaMedicamentoDAO {
    public List<Object[]> listarNoFacturadosPorPaciente(int idPaciente, Connection conn) throws SQLException {
        String sql = "SELECT em.id_entrega, m.precio_unitario * em.cantidad_entregada "
                   + "FROM entrega_medicamento em "
                   + "JOIN medicamento m ON em.id_medicamento = m.id_medicamento "
                   + "WHERE em.id_paciente = ? AND em.facturado = 0";
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
        String sql = "UPDATE entrega_medicamento SET facturado = 1 WHERE id_entrega = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReferencia);
            return ps.executeUpdate() == 1;
        }
    }

    public List<PendienteDTO> listarPendientesConPaciente() {
        List<PendienteDTO> lista = new ArrayList<>();
        String sql = "SELECT em.id_entrega, em.id_paciente, "
                   + "per.nombre || ' ' || per.apellido, "
                   + "m.nombre_comercial, "
                   + "m.precio_unitario * em.cantidad_entregada, "
                   + "em.fecha_entrega "
                   + "FROM entrega_medicamento em "
                   + "JOIN paciente p ON em.id_paciente = p.id_paciente "
                   + "JOIN persona per ON p.id_persona = per.id_persona "
                   + "JOIN medicamento m ON em.id_medicamento = m.id_medicamento "
                   + "WHERE em.facturado = 0 "
                   + "ORDER BY em.fecha_entrega DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idEntrega = rs.getInt(1);
                int idPac = rs.getInt(2);
                String paciente = rs.getString(3);
                String medName = rs.getString(4);
                double monto = rs.getDouble(5);
                String fecha = rs.getString(6);
                lista.add(new PendienteDTO(idPac, idEntrega, paciente,
                    "Medicamento: " + medName, monto, fecha, "MEDICAMENTO"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
