package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.model.Consulta;
import com.esperanza.hopecare.util.DatabaseConnection;
import java.sql.*;

public class ConsultaDAO {

    public int insertarConsultaYActualizarEstado(Consulta consulta) {
        String sqlInsert = "INSERT INTO consulta (id_cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta, precio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdate = "UPDATE cita SET estado = 'ATENDIDA' WHERE id_cita = ?";
        Connection conn = null;
        PreparedStatement pstmtInsert = null;
        PreparedStatement pstmtUpdate = null;
        try {
            conn = DatabaseConnection.getCitasUnifiedConnection();
            conn.setAutoCommit(false);

            pstmtInsert = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            pstmtInsert.setInt(1, consulta.getIdCita());
            pstmtInsert.setString(2, consulta.getDiagnostico());
            pstmtInsert.setString(3, consulta.getSintomas());
            pstmtInsert.setString(4, consulta.getTratamiento());
            pstmtInsert.setString(5, consulta.getNotasMedicas());
            pstmtInsert.setString(6, consulta.getFechaConsulta().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmtInsert.setDouble(7, consulta.getPrecio());
            int affectedInsert = pstmtInsert.executeUpdate();

            pstmtUpdate = conn.prepareStatement(sqlUpdate);
            pstmtUpdate.setInt(1, consulta.getIdCita());
            int affectedUpdate = pstmtUpdate.executeUpdate();

            if (affectedInsert == 1 && affectedUpdate == 1) {
                ResultSet rs = pstmtInsert.getGeneratedKeys();
                int idConsulta = -1;
                if (rs.next()) idConsulta = rs.getInt(1);
                consulta.setIdConsulta(idConsulta);
                conn.commit();
                return idConsulta;
            } else {
                conn.rollback();
                return -1;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (pstmtInsert != null) pstmtInsert.close();
                if (pstmtUpdate != null) pstmtUpdate.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Consulta obtenerConsultaPorId(int idConsulta) {
        String sql = "SELECT id_consulta, id_cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta, precio FROM consulta WHERE id_consulta = ?";
        try (Connection conn = DatabaseConnection.getCitasUnifiedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Consulta c = new Consulta();
                c.setIdConsulta(rs.getInt("id_consulta"));
                c.setIdCita(rs.getInt("id_cita"));
                c.setDiagnostico(rs.getString("diagnostico"));
                c.setSintomas(rs.getString("sintomas"));
                c.setTratamiento(rs.getString("tratamiento"));
                c.setNotasMedicas(rs.getString("notas_medicas"));
                c.setPrecio(rs.getDouble("precio"));
                String cf = rs.getString("fecha_consulta");
                if (cf != null) {
                    try {
                        c.setFechaConsulta(java.time.LocalDateTime.parse(cf, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } catch (Exception e) {
                        try { c.setFechaConsulta(java.time.LocalDateTime.parse(cf)); } catch (Exception ignored) {}
                    }
                }
                return c;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean actualizarConsulta(Consulta consulta) {
        String sql = "UPDATE consulta SET diagnostico = ?, sintomas = ?, tratamiento = ?, notas_medicas = ?, precio = ? WHERE id_consulta = ?";
        try (Connection conn = DatabaseConnection.getCitasUnifiedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, consulta.getDiagnostico());
            ps.setString(2, consulta.getSintomas());
            ps.setString(3, consulta.getTratamiento());
            ps.setString(4, consulta.getNotasMedicas());
            ps.setDouble(5, consulta.getPrecio());
            ps.setInt(6, consulta.getIdConsulta());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void insertarSiNoExiste(int idCita, double precio) {
        String sqlCheck = "SELECT COUNT(*) FROM consulta WHERE id_cita = ?";
        String sqlInsert = "INSERT INTO consulta (id_cita, diagnostico, sintomas, tratamiento, notas_medicas, fecha_consulta, precio) VALUES (?, '', '', '', '', NOW(), ?)";
        try (Connection conn = DatabaseConnection.getCitasUnifiedConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setInt(1, idCita);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, idCita);
                ps.setDouble(2, precio);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
