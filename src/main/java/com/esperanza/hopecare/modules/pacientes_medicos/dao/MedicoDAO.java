package com.esperanza.hopecare.modules.pacientes_medicos.dao;

import com.esperanza.hopecare.modules.pacientes_medicos.model.Medico;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAO {

    public List<Medico> listarTodos() {
        List<Medico> lista = new ArrayList<>();
        String sql = "SELECT m.id_medico, m.id_persona, m.id_especialidad, m.registro_medico, m.precio_consulta, m.activo, "
                   + "p.nombre, p.apellido, p.documento_identidad, p.fecha_nacimiento, p.telefono, p.email, p.direccion, p.genero, "
                   + "e.nombre_especialidad "
                   + "FROM medico m "
                   + "JOIN persona p ON m.id_persona = p.id_persona "
                   + "JOIN especialidad e ON m.id_especialidad = e.id_especialidad";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Medico m = new Medico();
                m.setIdMedico(rs.getInt("id_medico"));
                m.setIdPersona(rs.getInt("id_persona"));
                m.setIdEspecialidad(rs.getInt("id_especialidad"));
                m.setRegistroMedico(rs.getString("registro_medico"));
                m.setPrecioConsulta(rs.getDouble("precio_consulta"));
                m.setActivo(rs.getInt("activo") == 1);
                m.setNombre(rs.getString("nombre"));
                m.setApellido(rs.getString("apellido"));
                m.setDocumentoIdentidad(rs.getString("documento_identidad"));
                m.setFechaNacimiento(rs.getString("fecha_nacimiento"));
                m.setTelefono(rs.getString("telefono"));
                m.setEmail(rs.getString("email"));
                m.setDireccion(rs.getString("direccion"));
                m.setGenero(rs.getString("genero"));
                m.setNombreEspecialidad(rs.getString("nombre_especialidad"));
                lista.add(m);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return lista;
    }

    public boolean existeDocumento(String documento, int excluirIdPersona) {
        String sql = "SELECT COUNT(*) FROM persona WHERE documento_identidad = ? AND id_persona != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.setInt(2, excluirIdPersona);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean existeRegistroMedico(String registro, int excluirIdMedico) {
        String sql = "SELECT COUNT(*) FROM medico WHERE registro_medico = ? AND id_medico != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registro);
            ps.setInt(2, excluirIdMedico);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    public boolean insertarMedico(Medico m) {
        String sqlPersona = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlMedico = "INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta, activo) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement psP = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                psP.setString(1, m.getNombre() != null ? m.getNombre().trim() : "Sin nombre");
                psP.setString(2, m.getApellido() != null ? m.getApellido().trim() : "Sin apellido");
                psP.setString(3, m.getDocumentoIdentidad().trim());
                setStringOrNull(psP, 4, m.getFechaNacimiento());
                setStringOrNull(psP, 5, m.getTelefono());
                setStringOrNull(psP, 6, m.getEmail());
                setStringOrNull(psP, 7, m.getDireccion());
                setStringOrNull(psP, 8, m.getGenero());
                psP.executeUpdate();
                
                try (ResultSet rs = psP.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idPersona = rs.getInt(1);
                        m.setIdPersona(idPersona);
                    } else {
                        throw new SQLException("No se obtuvo el ID de persona generado.");
                    }
                }
            }
            
            try (PreparedStatement psM = conn.prepareStatement(sqlMedico, Statement.RETURN_GENERATED_KEYS)) {
                psM.setInt(1, m.getIdPersona());
                psM.setInt(2, m.getIdEspecialidad());
                psM.setString(3, m.getRegistroMedico().trim());
                psM.setDouble(4, m.getPrecioConsulta());
                psM.setInt(5, m.isActivo() ? 1 : 0);
                psM.executeUpdate();
                
                try (ResultSet rs = psM.getGeneratedKeys()) {
                    if (rs.next()) {
                        m.setIdMedico(rs.getInt(1));
                    }
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public boolean actualizarMedico(Medico m) {
        String sqlPersona = "UPDATE persona SET nombre = ?, apellido = ?, documento_identidad = ?, fecha_nacimiento = ?, telefono = ?, email = ?, direccion = ?, genero = ? WHERE id_persona = ?";
        String sqlMedico = "UPDATE medico SET id_especialidad = ?, registro_medico = ?, precio_consulta = ?, activo = ? WHERE id_medico = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement psP = conn.prepareStatement(sqlPersona)) {
                psP.setString(1, m.getNombre().trim());
                psP.setString(2, m.getApellido().trim());
                psP.setString(3, m.getDocumentoIdentidad().trim());
                setStringOrNull(psP, 4, m.getFechaNacimiento());
                setStringOrNull(psP, 5, m.getTelefono());
                setStringOrNull(psP, 6, m.getEmail());
                setStringOrNull(psP, 7, m.getDireccion());
                setStringOrNull(psP, 8, m.getGenero());
                psP.setInt(9, m.getIdPersona());
                psP.executeUpdate();
            }
            
            try (PreparedStatement psM = conn.prepareStatement(sqlMedico)) {
                psM.setInt(1, m.getIdEspecialidad());
                psM.setString(2, m.getRegistroMedico().trim());
                psM.setDouble(3, m.getPrecioConsulta());
                psM.setInt(4, m.isActivo() ? 1 : 0);
                psM.setInt(5, m.getIdMedico());
                psM.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public boolean eliminarMedicoLogico(int idMedico) {
        String sql = "UPDATE medico SET activo = 0 WHERE id_medico = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}