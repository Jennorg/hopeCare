package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.model.FacturaResumenDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FacturaDAO {
    public int insertarFactura(int idPaciente, double subtotal, double impuesto, double total, String estadoPago, Connection conn) throws SQLException {
        String sql = "INSERT INTO factura (id_paciente, fecha_emision, subtotal, impuesto, total, estado_pago) VALUES (?, NOW(), ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPaciente);
            ps.setDouble(2, subtotal);
            ps.setDouble(3, impuesto);
            ps.setDouble(4, total);
            ps.setString(5, estadoPago);
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public List<FacturaResumenDTO> listarTodasConPaciente() {
        List<FacturaResumenDTO> lista = new ArrayList<>();
        // clinica es el alias del ATTACH
        String sql = "SELECT f.id_factura, f.fecha_emision, f.subtotal, f.impuesto, f.total, f.estado_pago, "
                   + "per.nombre, per.apellido "
                   + "FROM factura f "
                   + "JOIN hopecare_clinica.paciente p ON f.id_paciente = p.id_paciente "
                   + "JOIN hopecare_clinica.persona per ON p.id_persona = per.id_persona "
                   + "ORDER BY f.fecha_emision DESC";
        try (Connection conn = DatabaseConnection.getFacturacionUnifiedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String paciente = rs.getString("nombre") + " " + rs.getString("apellido");
                lista.add(new FacturaResumenDTO(
                    rs.getInt("id_factura"),
                    paciente,
                    rs.getString("fecha_emision"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("impuesto"),
                    rs.getDouble("total"),
                    rs.getString("estado_pago")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizarEstadoPago(int idFactura, String nuevoEstado) {
        String sql = "UPDATE factura SET estado_pago = ? WHERE id_factura = ?";
        try (Connection conn = DatabaseConnection.getFacturacionConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idFactura);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FacturaResumenDTO> listarPorIdPaciente(int idPaciente) {
        List<FacturaResumenDTO> lista = new ArrayList<>();
        String sql = "SELECT f.id_factura, f.fecha_emision, f.subtotal, f.impuesto, f.total, f.estado_pago, "
                   + "per.nombre, per.apellido "
                   + "FROM factura f "
                   + "JOIN hopecare_clinica.paciente p ON f.id_paciente = p.id_paciente "
                   + "JOIN hopecare_clinica.persona per ON p.id_persona = per.id_persona "
                   + "WHERE f.id_paciente = ? "
                   + "ORDER BY f.fecha_emision DESC";
        try (Connection conn = DatabaseConnection.getFacturacionUnifiedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String paciente = rs.getString("nombre") + " " + rs.getString("apellido");
                lista.add(new FacturaResumenDTO(
                    rs.getInt("id_factura"),
                    paciente,
                    rs.getString("fecha_emision"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("impuesto"),
                    rs.getDouble("total"),
                    rs.getString("estado_pago")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean eliminarFactura(int idFactura) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getFacturacionUnifiedConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE hopecare_citas.consulta SET facturado = 0 WHERE id_consulta IN ("
                    + "SELECT id_referencia FROM detalle_factura WHERE id_factura = ? AND tipo_referencia = 'CONSULTA')")) {
                ps.setInt(1, idFactura);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM detalle_factura WHERE id_factura = ?")) {
                ps.setInt(1, idFactura);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM factura WHERE id_factura = ?")) {
                ps.setInt(1, idFactura);
                int affected = ps.executeUpdate();
                if (affected == 0) { conn.rollback(); return false; }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            return false;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    public Set<Integer> obtenerIdsPacientesConPendientes() {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT DISTINCT ci.id_paciente FROM hopecare_citas.consulta c JOIN hopecare_citas.cita ci ON c.id_cita = ci.id_cita WHERE ci.estado = 'ATENDIDA' AND c.facturado = 0";
        try (Connection conn = DatabaseConnection.getFacturacionUnifiedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
}
