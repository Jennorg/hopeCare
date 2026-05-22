package com.esperanza.hopecare.modules.pacientes_medicos.dao;

import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT p.id_paciente, p.id_persona, p.historia_clinica, p.alergias, p.grupo_sanguineo, p.contacto_emergencia, "
                   + "per.nombre, per.apellido, per.documento_identidad, per.fecha_nacimiento, per.telefono, per.email, per.direccion, per.genero "
                   + "FROM paciente p "
                   + "JOIN persona per ON p.id_persona = per.id_persona";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Paciente p = new Paciente();
                p.setIdPaciente(rs.getInt("id_paciente"));
                p.setIdPersona(rs.getInt("id_persona"));
                p.setHistoriaClinica(rs.getString("historia_clinica"));
                p.setAlergias(rs.getString("alergias"));
                p.setGrupoSanguineo(rs.getString("grupo_sanguineo"));
                p.setContactoEmergencia(rs.getString("contacto_emergencia"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setDocumentoIdentidad(rs.getString("documento_identidad"));
                p.setFechaNacimiento(rs.getString("fecha_nacimiento"));
                p.setTelefono(rs.getString("telefono"));
                p.setEmail(rs.getString("email"));
                p.setDireccion(rs.getString("direccion"));
                p.setGenero(rs.getString("genero"));
                lista.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
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

    public boolean existeHistoriaClinica(String historiaClinica, int excluirIdPaciente) {
        String sql = "SELECT COUNT(*) FROM paciente WHERE historia_clinica = ? AND id_paciente != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, historiaClinica);
            ps.setInt(2, excluirIdPaciente);
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

    public int obtenerIdPacientePorIdPersona(int idPersona) {
        String sql = "SELECT id_paciente FROM paciente WHERE id_persona = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_paciente");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean insertar(Paciente p) {
        String sqlPersona = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPaciente = "INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psP = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                psP.setString(1, p.getNombre().trim());
                psP.setString(2, p.getApellido().trim());
                psP.setString(3, p.getDocumentoIdentidad().trim());
                setStringOrNull(psP, 4, p.getFechaNacimiento());
                setStringOrNull(psP, 5, p.getTelefono());
                setStringOrNull(psP, 6, p.getEmail());
                setStringOrNull(psP, 7, p.getDireccion());
                setStringOrNull(psP, 8, p.getGenero());
                psP.executeUpdate();

                try (ResultSet rs = psP.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idPersona = rs.getInt(1);
                        p.setIdPersona(idPersona);
                    } else {
                        throw new SQLException("No se obtuvo el ID generado para la persona.");
                    }
                }
            }

            try (PreparedStatement psPac = conn.prepareStatement(sqlPaciente, Statement.RETURN_GENERATED_KEYS)) {
                psPac.setInt(1, p.getIdPersona());
                psPac.setString(2, p.getHistoriaClinica().trim());
                setStringOrNull(psPac, 3, p.getAlergias());
                setStringOrNull(psPac, 4, p.getGrupoSanguineo());
                setStringOrNull(psPac, 5, p.getContactoEmergencia());
                psPac.executeUpdate();

                try (ResultSet rs = psPac.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setIdPaciente(rs.getInt(1));
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
}
