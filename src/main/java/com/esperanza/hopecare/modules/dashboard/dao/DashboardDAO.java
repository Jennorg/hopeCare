package com.esperanza.hopecare.modules.dashboard.dao;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.dashboard.model.DashboardData;
import java.sql.*;
import java.util.*;

public class DashboardDAO {

    public DashboardData cargarTodosLosDatos() {
        DashboardData data = new DashboardData();
        try (Connection conn = DatabaseConnection.getConnection()) {
            data.setCitasHoy(obtenerCitasDelDia(conn));
            data.setPacientesAtendidosHoy(obtenerPacientesAtendidosHoy(conn));
            data.setIngresosMes(obtenerIngresosDelMes(conn));
            data.setPorcentajeAsistencia(obtenerPorcentajeAsistencia(conn));
            data.setEstadoCitas(obtenerConteoPorEstado(conn));
            data.setMedicamentosStockBajo(obtenerMedicamentosStockBajo(conn));
            data.setSolicitudesLabPendientes(obtenerSolicitudesLabPendientes(conn));
            data.setFacturasPendientes(obtenerFacturasPendientes(conn));
            data.setIngresosConsultas(obtenerIngresosPorTipo(conn, "CONSULTA"));
            data.setIngresosFarmacia(obtenerIngresosPorTipo(conn, "MEDICAMENTO"));
            data.setIngresosLaboratorio(obtenerIngresosPorTipo(conn, "EXAMEN"));
            data.setRegistrosRecientes(obtenerRegistrosRecientes(conn));
            data.setTotalPacientes(obtenerTotalPacientes(conn));
            data.setTotalMedicos(obtenerTotalMedicosActivos(conn));
            data.setTotalMedicamentos(obtenerTotalMedicamentos(conn));
            data.setTotalExamenes(obtenerTotalExamenes(conn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private int obtenerCitasDelDia(Connection conn) {
        String sql = "SELECT COUNT(*) FROM cita WHERE DATE(fecha_hora) = DATE('now')";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int obtenerPacientesAtendidosHoy(Connection conn) {
        String sql = "SELECT COUNT(DISTINCT c.id_paciente) FROM consulta co " +
                     "JOIN cita c ON co.id_cita = c.id_cita " +
                     "WHERE DATE(co.fecha_consulta) = DATE('now')";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private double obtenerIngresosDelMes(Connection conn) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM factura " +
                     "WHERE strftime('%Y-%m', fecha_emision) = strftime('%Y-%m', 'now') " +
                     "AND estado_pago = 'PAGADO'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private double obtenerPorcentajeAsistencia(Connection conn) {
        String sql = "SELECT COUNT(*) as total, " +
                     "COALESCE(SUM(CASE WHEN estado = 'ATENDIDA' THEN 1 ELSE 0 END), 0) as atendidas " +
                     "FROM cita WHERE strftime('%Y-%m', fecha_hora) = strftime('%Y-%m', 'now')";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int atendidas = rs.getInt("atendidas");
                return total > 0 ? (atendidas * 100.0 / total) : 0.0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private Map<String, Integer> obtenerConteoPorEstado(Connection conn) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        String sql = "SELECT estado, COUNT(*) as cantidad FROM cita " +
                     "WHERE strftime('%Y-%m', fecha_hora) = strftime('%Y-%m', 'now') " +
                     "GROUP BY estado ORDER BY estado";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapa.put(rs.getString("estado"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mapa;
    }

    private List<String> obtenerMedicamentosStockBajo(Connection conn) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_comercial FROM medicamento WHERE stock_actual < stock_minimo";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(rs.getString("nombre_comercial"));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private int obtenerSolicitudesLabPendientes(Connection conn) {
        String sql = "SELECT COUNT(*) FROM solicitud_examen WHERE estado = 'PENDIENTE'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int obtenerFacturasPendientes(Connection conn) {
        String sql = "SELECT COUNT(*) FROM factura WHERE estado_pago != 'PAGADO'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private double obtenerIngresosPorTipo(Connection conn, String tipoReferencia) {
        String sql = "SELECT COALESCE(SUM(df.monto), 0) FROM detalle_factura df " +
                     "JOIN factura f ON df.id_factura = f.id_factura " +
                     "WHERE strftime('%Y-%m', f.fecha_emision) = strftime('%Y-%m', 'now') " +
                     "AND f.estado_pago = 'PAGADO' AND df.tipo_referencia = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipoReferencia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private List<String> obtenerRegistrosRecientes(Connection conn) {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT fecha, descripcion FROM (" +
                     "SELECT fecha_hora as fecha, 'Nueva cita programada' as descripcion FROM cita " +
                     "UNION ALL " +
                     "SELECT co.fecha_consulta, 'Consulta realizada' FROM consulta co " +
                     "UNION ALL " +
                     "SELECT em.fecha_entrega, 'Entrega de farmacia' FROM entrega_medicamento em " +
                     "UNION ALL " +
                     "SELECT se.fecha_solicitud, 'Solicitud de laboratorio' FROM solicitud_examen se " +
                     "UNION ALL " +
                     "SELECT f.fecha_emision, 'Factura emitida' FROM factura f " +
                     ") ORDER BY fecha DESC LIMIT 10";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String fecha = rs.getString("fecha");
                String desc = rs.getString("descripcion");
                if (fecha != null && fecha.length() > 16) fecha = fecha.substring(0, 16);
                lista.add((fecha != null ? fecha : "--") + " - " + desc);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private int obtenerTotalPacientes(Connection conn) {
        String sql = "SELECT COUNT(*) FROM paciente";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int obtenerTotalMedicosActivos(Connection conn) {
        String sql = "SELECT COUNT(*) FROM medico WHERE activo = 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int obtenerTotalMedicamentos(Connection conn) {
        String sql = "SELECT COUNT(*) FROM medicamento";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int obtenerTotalExamenes(Connection conn) {
        String sql = "SELECT COUNT(*) FROM examen_laboratorio";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Métodos públicos individuales compatibles con usos existentes

    public int obtenerCitasDelDia() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return obtenerCitasDelDia(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double obtenerIngresosDelMes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return obtenerIngresosDelMes(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public List<String> obtenerMedicamentosStockBajo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return obtenerMedicamentosStockBajo(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return Collections.emptyList();
    }
}
