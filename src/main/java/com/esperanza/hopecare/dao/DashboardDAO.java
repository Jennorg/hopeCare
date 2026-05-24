package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.model.DashboardData;
import com.esperanza.hopecare.model.RegistroItem;
import java.sql.*;
import java.util.*;

public class DashboardDAO {

    public DashboardData cargarTodosLosDatos() {
        DashboardData data = new DashboardData();
        try (Connection conn = DatabaseConnection.getDashboardConnection()) {
            data.setCitasHoy(obtenerCitasDelDia(conn));
            data.setPacientesAtendidosHoy(obtenerPacientesAtendidosHoy(conn));
            data.setIngresosMes(obtenerIngresosDelMes(conn));
            data.setPorcentajeAsistencia(obtenerPorcentajeAsistencia(conn));
            data.setEstadoCitas(obtenerConteoPorEstado(conn));
            data.setMedicamentosStockBajo(obtenerMedicamentosStockBajo(conn));
            data.setSolicitudesLabPendientes(obtenerSolicitudesLabPendientes(conn));
            data.setFacturasPendientes(obtenerFacturasPendientes(conn));
            data.setIngresosConsultas(obtenerIngresosConsultas(conn));
            data.setIngresosFarmacia(obtenerIngresosPorTipo(conn, "MEDICAMENTO"));
            data.setIngresosLaboratorio(obtenerIngresosPorTipo(conn, "EXAMEN"));
            data.setRegistrosRecientes(obtenerRegistrosRecientes(conn));
            data.setTotalPacientes(obtenerTotalPacientes(conn));
            data.setTotalMedicos(obtenerTotalMedicosActivos(conn));
            data.setTotalMedicamentos(obtenerTotalMedicamentos(conn));
            data.setTotalExamenes(obtenerTotalExamenes(conn));
            data.setListaPacientes(obtenerListaPacientes(conn));
            data.setListaMedicos(obtenerListaMedicos(conn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
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
        String sql = "SELECT nombre_comercial FROM medicamento WHERE stock_actual = 0";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(rs.getString("nombre_comercial"));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private int obtenerSolicitudesLabPendientes(Connection conn) {
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

    private double obtenerIngresosConsultas(Connection conn) {
        return 0.0;
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

    private List<RegistroItem> obtenerRegistrosRecientes(Connection conn) {
        List<RegistroItem> lista = new ArrayList<>();
        String sql = "SELECT fecha, descripcion, tipo, id FROM (" +
                     "SELECT c.fecha_hora as fecha, 'Nueva cita: ' || p.nombre || ' ' || p.apellido || ' con ' || m2.nombre || ' ' || m2.apellido as descripcion, 'CITA' as tipo, c.id_cita as id FROM cita c " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "JOIN medico m ON c.id_medico = m.id_medico " +
                     "JOIN persona m2 ON m.id_persona = m2.id_persona " +
                     "UNION ALL " +
                     "SELECT co.fecha_consulta, 'Consulta: ' || p.nombre || ' ' || p.apellido, 'CONSULTA' as tipo, co.id_consulta FROM consulta co " +
                     "JOIN cita c ON co.id_cita = c.id_cita " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "UNION ALL " +
                     "SELECT f.fecha_emision, 'Factura #' || f.id_factura || ': $' || f.total || ' (' || f.estado_pago || ')', 'FACTURA' as tipo, f.id_factura FROM factura f " +
                     ") ORDER BY fecha DESC LIMIT 10";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String fecha = rs.getString("fecha");
                String desc = rs.getString("descripcion");
                String tipo = rs.getString("tipo");
                int id = rs.getInt("id");
                if (fecha != null && fecha.length() > 16) fecha = fecha.substring(0, 16);
                lista.add(new RegistroItem((fecha != null ? fecha : "--") + " - " + desc, tipo, id));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
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
        return 0;
    }

    public List<String[]> obtenerDetalleCitasHoy() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, p.apellido, m2.nombre as med_nombre, m2.apellido as med_apellido, " +
                     "c.fecha_hora, c.estado, c.motivo " +
                     "FROM cita c " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "JOIN medico m ON c.id_medico = m.id_medico " +
                     "JOIN persona m2 ON m.id_persona = m2.id_persona " +
                     "WHERE DATE(c.fecha_hora) = DATE('now') ORDER BY c.fecha_hora";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nombre") + " " + rs.getString("apellido"),
                    rs.getString("med_nombre") + " " + rs.getString("med_apellido"),
                    rs.getString("fecha_hora"),
                    rs.getString("estado"),
                    rs.getString("motivo")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<String[]> obtenerDetalleAtendidosHoy() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, p.apellido, c.fecha_hora, co.diagnostico " +
                     "FROM consulta co " +
                     "JOIN cita c ON co.id_cita = c.id_cita " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "WHERE DATE(co.fecha_consulta) = DATE('now') ORDER BY c.fecha_hora";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nombre") + " " + rs.getString("apellido"),
                    rs.getString("fecha_hora"),
                    rs.getString("diagnostico")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<String[]> obtenerDetalleFacturas() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT f.id_factura, p.nombre, p.apellido, f.fecha_emision, f.total, f.estado_pago, f.forma_pago " +
                     "FROM factura f " +
                     "JOIN paciente pac ON f.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "ORDER BY f.fecha_emision DESC LIMIT 20";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new String[]{
                    "FAC-" + rs.getInt("id_factura"),
                    rs.getString("nombre") + " " + rs.getString("apellido"),
                    rs.getString("fecha_emision"),
                    String.format("$%,.0f", rs.getDouble("total")),
                    rs.getString("estado_pago"),
                    rs.getString("forma_pago") != null ? rs.getString("forma_pago") : ""
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<String[]> obtenerDetalleConsultas() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, p.apellido, m2.nombre as med_nombre, m2.apellido as med_apellido, " +
                     "c.fecha_hora, co.diagnostico, '' as precio " +
                     "FROM consulta co " +
                     "JOIN cita c ON co.id_cita = c.id_cita " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "JOIN medico m ON c.id_medico = m.id_medico " +
                     "JOIN persona m2 ON m.id_persona = m2.id_persona " +
                     "ORDER BY c.fecha_hora DESC LIMIT 20";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nombre") + " " + rs.getString("apellido"),
                    rs.getString("med_nombre") + " " + rs.getString("med_apellido"),
                    rs.getString("fecha_hora"),
                    rs.getString("diagnostico") != null ? rs.getString("diagnostico") : "",
                    ""
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public String[] obtenerDetalleCompletoCita(int idCita) {
        String sql = "SELECT c.id_cita, p.nombre as pac_nombre, p.apellido as pac_apellido, " +
                     "m2.nombre as med_nombre, m2.apellido as med_apellido, e.nombre_especialidad, " +
                     "c.fecha_hora, c.estado, c.motivo, u.nombre_usuario, c.fecha_creacion " +
                     "FROM cita c " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "JOIN medico m ON c.id_medico = m.id_medico " +
                     "JOIN persona m2 ON m.id_persona = m2.id_persona " +
                     "JOIN especialidad e ON m.id_especialidad = e.id_especialidad " +
                     "JOIN usuario u ON c.creada_por = u.id_usuario " +
                     "WHERE c.id_cita = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        "CITA #" + rs.getInt("id_cita"),
                        "Paciente: " + rs.getString("pac_nombre") + " " + rs.getString("pac_apellido"),
                        "M\u00e9dico: " + rs.getString("med_nombre") + " " + rs.getString("med_apellido") + " (" + rs.getString("nombre_especialidad") + ")",
                        "Fecha/Hora: " + rs.getString("fecha_hora"),
                        "Estado: " + rs.getString("estado"),
                        "Motivo: " + (rs.getString("motivo") != null ? rs.getString("motivo") : ""),
                        "Registrada por: " + rs.getString("nombre_usuario"),
                        "Fecha creaci\u00f3n: " + rs.getString("fecha_creacion")
                    };
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new String[]{"No se encontr\u00f3 la cita."};
    }

    public String[] obtenerDetalleCompletoConsulta(int idConsulta) {
        String sql = "SELECT co.id_consulta, p.nombre as pac_nombre, p.apellido as pac_apellido, " +
                     "m2.nombre as med_nombre, m2.apellido as med_apellido, " +
                     "c.fecha_hora, co.diagnostico, co.sintomas, co.tratamiento, co.notas_medicas, " +
                     "co.fecha_consulta, co.facturado " +
                     "FROM consulta co " +
                     "JOIN cita c ON co.id_cita = c.id_cita " +
                     "JOIN paciente pac ON c.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "JOIN medico m ON c.id_medico = m.id_medico " +
                     "JOIN persona m2 ON m.id_persona = m2.id_persona " +
                     "WHERE co.id_consulta = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        "CONSULTA #" + rs.getInt("id_consulta"),
                        "Paciente: " + rs.getString("pac_nombre") + " " + rs.getString("pac_apellido"),
                        "M\u00e9dico: " + rs.getString("med_nombre") + " " + rs.getString("med_apellido"),
                        "Fecha cita: " + rs.getString("fecha_hora"),
                        "Fecha consulta: " + rs.getString("fecha_consulta"),
                        "Diagn\u00f3stico: " + (rs.getString("diagnostico") != null ? rs.getString("diagnostico") : ""),
                        "S\u00edntomas: " + (rs.getString("sintomas") != null ? rs.getString("sintomas") : ""),
                        "Tratamiento: " + (rs.getString("tratamiento") != null ? rs.getString("tratamiento") : ""),
                        "Notas: " + (rs.getString("notas_medicas") != null ? rs.getString("notas_medicas") : ""),
                        "Facturado: " + (rs.getBoolean("facturado") ? "S\u00cd" : "NO")
                    };
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new String[]{"No se encontr\u00f3 la consulta."};
    }

    public String[] obtenerDetalleCompletoFactura(int idFactura) {
        String sql = "SELECT f.id_factura, p.nombre, p.apellido, f.fecha_emision, f.subtotal, f.impuesto, f.total, f.estado_pago, f.forma_pago " +
                     "FROM factura f " +
                     "JOIN paciente pac ON f.id_paciente = pac.id_paciente " +
                     "JOIN persona p ON pac.id_persona = p.id_persona " +
                     "WHERE f.id_factura = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String facturaLine = "FACTURA #" + rs.getInt("id_factura");
                    String pacLine = "Paciente: " + rs.getString("nombre") + " " + rs.getString("apellido");
                    String fechaLine = "Fecha: " + rs.getString("fecha_emision");
                    String subLine = "Subtotal: $" + String.format("%,.0f", rs.getDouble("subtotal"));
                    String impLine = "Impuesto: $" + String.format("%,.0f", rs.getDouble("impuesto"));
                    String totalLine = "TOTAL: $" + String.format("%,.0f", rs.getDouble("total"));
                    String estadoLine = "Estado: " + rs.getString("estado_pago");
                    String pagoLine = "Forma de pago: " + (rs.getString("forma_pago") != null ? rs.getString("forma_pago") : "");

                    List<String> lineas = new ArrayList<>(Arrays.asList(facturaLine, pacLine, fechaLine, subLine, impLine, totalLine, estadoLine, pagoLine));

                    lineas.add("");
                    lineas.add("--- DETALLES ---");
                    String detSql = "SELECT concepto, monto FROM detalle_factura WHERE id_factura = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(detSql)) {
                        ps2.setInt(1, idFactura);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                lineas.add("  " + rs2.getString("concepto") + " - $" + String.format("%,.0f", rs2.getDouble("monto")));
                            }
                        }
                    }
                    return lineas.toArray(new String[0]);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new String[]{"No se encontr\u00f3 la factura."};
    }

    private List<RegistroItem> obtenerListaPacientes(Connection conn) {
        List<RegistroItem> lista = new ArrayList<>();
        String sql = "SELECT pac.id_paciente, p.nombre, p.apellido, p.documento_identidad, pac.historia_clinica " +
                     "FROM paciente pac JOIN persona p ON pac.id_persona = p.id_persona " +
                     "ORDER BY p.apellido, p.nombre";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String display = rs.getString("nombre") + " " + rs.getString("apellido") +
                          " | Doc: " + rs.getString("documento_identidad") +
                          " | HC: " + rs.getString("historia_clinica");
                lista.add(new RegistroItem(display, "PACIENTE", rs.getInt("id_paciente")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private List<RegistroItem> obtenerListaMedicos(Connection conn) {
        List<RegistroItem> lista = new ArrayList<>();
        String sql = "SELECT m.id_medico, p.nombre, p.apellido, p.documento_identidad, e.nombre_especialidad, m.registro_medico " +
                     "FROM medico m " +
                     "JOIN persona p ON m.id_persona = p.id_persona " +
                     "JOIN especialidad e ON m.id_especialidad = e.id_especialidad " +
                     "WHERE m.activo = 1 ORDER BY p.apellido, p.nombre";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String display = rs.getString("nombre") + " " + rs.getString("apellido") +
                          " | " + rs.getString("nombre_especialidad") +
                          " | Reg: " + rs.getString("registro_medico");
                lista.add(new RegistroItem(display, "MEDICO", rs.getInt("id_medico")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<RegistroItem> obtenerListaPacientes() {
        try (Connection conn = DatabaseConnection.getDashboardConnection()) {
            return obtenerListaPacientes(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return Collections.emptyList();
    }

    public List<RegistroItem> obtenerListaMedicos() {
        try (Connection conn = DatabaseConnection.getDashboardConnection()) {
            return obtenerListaMedicos(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return Collections.emptyList();
    }

    public String[] obtenerDetalleCompletoPaciente(int id) {
        String sql = "SELECT p.nombre, p.apellido, p.documento_identidad, p.fecha_nacimiento, " +
                     "p.telefono, p.email, p.direccion, p.genero, " +
                     "pac.historia_clinica, pac.alergias, pac.grupo_sanguineo, " +
                     "pac.contacto_emergencia, pac.fecha_registro " +
                     "FROM paciente pac JOIN persona p ON pac.id_persona = p.id_persona " +
                     "WHERE pac.id_paciente = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        "PACIENTE",
                        "Nombre: " + rs.getString("nombre") + " " + rs.getString("apellido"),
                        "Documento: " + rs.getString("documento_identidad"),
                        "Fecha Nac.: " + (rs.getString("fecha_nacimiento") != null ? rs.getString("fecha_nacimiento") : ""),
                        "G\u00e9nero: " + (rs.getString("genero") != null ? rs.getString("genero") : ""),
                        "Tel\u00e9fono: " + (rs.getString("telefono") != null ? rs.getString("telefono") : ""),
                        "Email: " + (rs.getString("email") != null ? rs.getString("email") : ""),
                        "Direcci\u00f3n: " + (rs.getString("direccion") != null ? rs.getString("direccion") : ""),
                        "Historia Cl\u00ednica: " + rs.getString("historia_clinica"),
                        "Alergias: " + (rs.getString("alergias") != null ? rs.getString("alergias") : "Ninguna"),
                        "Grupo Sangu\u00edneo: " + (rs.getString("grupo_sanguineo") != null ? rs.getString("grupo_sanguineo") : ""),
                        "Contacto Emergencia: " + (rs.getString("contacto_emergencia") != null ? rs.getString("contacto_emergencia") : ""),
                        "Fecha Registro: " + (rs.getString("fecha_registro") != null ? rs.getString("fecha_registro") : "")
                    };
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new String[]{"No se encontr\u00f3 el paciente."};
    }

    public String[] obtenerDetalleCompletoMedico(int id) {
        String sql = "SELECT p.nombre, p.apellido, p.documento_identidad, p.fecha_nacimiento, " +
                     "p.telefono, p.email, p.direccion, p.genero, " +
                     "e.nombre_especialidad, m.registro_medico " +
                     "FROM medico m " +
                     "JOIN persona p ON m.id_persona = p.id_persona " +
                     "JOIN especialidad e ON m.id_especialidad = e.id_especialidad " +
                     "WHERE m.id_medico = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        "M\u00c9DICO",
                        "Nombre: " + rs.getString("nombre") + " " + rs.getString("apellido"),
                        "Documento: " + rs.getString("documento_identidad"),
                        "Fecha Nac.: " + (rs.getString("fecha_nacimiento") != null ? rs.getString("fecha_nacimiento") : ""),
                        "G\u00e9nero: " + (rs.getString("genero") != null ? rs.getString("genero") : ""),
                        "Tel\u00e9fono: " + (rs.getString("telefono") != null ? rs.getString("telefono") : ""),
                        "Email: " + (rs.getString("email") != null ? rs.getString("email") : ""),
                        "Direcci\u00f3n: " + (rs.getString("direccion") != null ? rs.getString("direccion") : ""),
                        "Especialidad: " + rs.getString("nombre_especialidad"),
                        "Registro M\u00e9dico: " + rs.getString("registro_medico")
                    };
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new String[]{"No se encontr\u00f3 el m\u00e9dico."};
    }

    public boolean eliminarPaciente(int id) {
        String sql = "DELETE FROM paciente WHERE id_paciente = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean eliminarMedico(int id) {
        String sql = "UPDATE medico SET activo = 0 WHERE id_medico = ?";
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int obtenerCitasDelDia() {
        try (Connection conn = DatabaseConnection.getDashboardConnection()) {
            return obtenerCitasDelDia(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double obtenerIngresosDelMes() {
        try (Connection conn = DatabaseConnection.getDashboardConnection()) {
            return obtenerIngresosDelMes(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public List<String> obtenerMedicamentosStockBajo() {
        try (Connection conn = DatabaseConnection.getDashboardConnection()) {
            return obtenerMedicamentosStockBajo(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return Collections.emptyList();
    }
}
