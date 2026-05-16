package com.esperanza.hopecare.modules.medicamentos_lab.dao;

import com.esperanza.hopecare.modules.medicamentos_lab.model.EntregaMedicamento;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntregaMedicamentoDAO {

    public boolean insertar(EntregaMedicamento entrega, Connection conn) throws SQLException {
        String sql = "INSERT INTO entrega_medicamento (id_paciente, id_medicamento, cantidad_entregada, presente_receta, entregado_por, facturado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entrega.getIdPaciente());
            ps.setInt(2, entrega.getIdMedicamento());
            ps.setInt(3, entrega.getCantidadEntregada());
            ps.setBoolean(4, entrega.isPresenteReceta());
            ps.setInt(5, entrega.getEntregadoPor());
            ps.setBoolean(6, entrega.isFacturado());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean marcarFacturado(int idEntrega, Connection conn) throws SQLException {
        String sql = "UPDATE entrega_medicamento SET facturado = 1 WHERE id_entrega = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEntrega);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int idEntrega, Connection conn) throws SQLException {
        EntregaMedicamento entrega = obtenerPorId(idEntrega, conn);
        if (entrega == null) return false;

        if (entrega.isFacturado()) {
            throw new SQLException("No se puede eliminar una entrega facturada");
        }

        int idMedicamento = entrega.getIdMedicamento();
        int cantidadEntregada = entrega.getCantidadEntregada();

        String sqlStock = "SELECT stock_actual FROM medicamento WHERE id_medicamento = ?";
        int stockActual = 0;
        try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
            ps.setInt(1, idMedicamento);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stockActual = rs.getInt("stock_actual");
            }
        }

        String sqlUpdateStock = "UPDATE medicamento SET stock_actual = ? WHERE id_medicamento = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdateStock)) {
            ps.setInt(1, stockActual + cantidadEntregada);
            ps.setInt(2, idMedicamento);
            ps.executeUpdate();
        }

        String sqlDelete = "DELETE FROM entrega_medicamento WHERE id_entrega = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
            ps.setInt(1, idEntrega);
            return ps.executeUpdate() == 1;
        }
    }

    private EntregaMedicamento obtenerPorId(int idEntrega, Connection conn) throws SQLException {
        String sql = "SELECT e.id_entrega, e.id_paciente, e.id_medicamento, e.cantidad_entregada, e.presente_receta, e.entregado_por, e.fecha_entrega, e.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, m.nombre_comercial as medicamento_nombre " +
                     "FROM entrega_medicamento e " +
                     "JOIN paciente pc ON e.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN medicamento m ON e.id_medicamento = m.id_medicamento " +
                     "WHERE e.id_entrega = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEntrega);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    public List<EntregaMedicamento> listarTodas() {
        List<EntregaMedicamento> lista = new ArrayList<>();
        String sql = "SELECT e.id_entrega, e.id_paciente, e.id_medicamento, e.cantidad_entregada, e.presente_receta, e.entregado_por, e.fecha_entrega, e.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, m.nombre_comercial as medicamento_nombre " +
                     "FROM entrega_medicamento e " +
                     "JOIN paciente pc ON e.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN medicamento m ON e.id_medicamento = m.id_medicamento " +
                     "ORDER BY e.fecha_entrega DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<EntregaMedicamento> listarPorPaciente(int idPaciente) {
        List<EntregaMedicamento> lista = new ArrayList<>();
        String sql = "SELECT e.id_entrega, e.id_paciente, e.id_medicamento, e.cantidad_entregada, e.presente_receta, e.entregado_por, e.fecha_entrega, e.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, m.nombre_comercial as medicamento_nombre " +
                     "FROM entrega_medicamento e " +
                     "JOIN paciente pc ON e.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN medicamento m ON e.id_medicamento = m.id_medicamento " +
                     "WHERE e.id_paciente = ? ORDER BY e.fecha_entrega DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<EntregaMedicamento> listarPorMedicamento(int idMedicamento) {
        List<EntregaMedicamento> lista = new ArrayList<>();
        String sql = "SELECT e.id_entrega, e.id_paciente, e.id_medicamento, e.cantidad_entregada, e.presente_receta, e.entregado_por, e.fecha_entrega, e.facturado, p.nombre as paciente_nombre, p.apellido as paciente_apellido, m.nombre_comercial as medicamento_nombre " +
                     "FROM entrega_medicamento e " +
                     "JOIN paciente pc ON e.id_paciente = pc.id_paciente " +
                     "JOIN persona p ON pc.id_persona = p.id_persona " +
                     "JOIN medicamento m ON e.id_medicamento = m.id_medicamento " +
                     "WHERE e.id_medicamento = ? ORDER BY e.fecha_entrega DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicamento);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private EntregaMedicamento mapear(ResultSet rs) throws SQLException {
        EntregaMedicamento e = new EntregaMedicamento();
        e.setIdEntrega(rs.getInt("id_entrega"));
        e.setIdPaciente(rs.getInt("id_paciente"));
        e.setIdMedicamento(rs.getInt("id_medicamento"));
        e.setCantidadEntregada(rs.getInt("cantidad_entregada"));
        e.setPresenteReceta(rs.getBoolean("presente_receta"));
        e.setEntregadoPor(rs.getInt("entregado_por"));
        e.setFacturado(rs.getBoolean("facturado"));
        Timestamp ts = rs.getTimestamp("fecha_entrega");
        if (ts != null) {
            e.setFechaEntrega(ts.toLocalDateTime());
        }
        e.setPacienteNombre(rs.getString("paciente_nombre"));
        e.setPacienteApellido(rs.getString("paciente_apellido"));
        e.setMedicamentoNombre(rs.getString("medicamento_nombre"));
        return e;
    }
}