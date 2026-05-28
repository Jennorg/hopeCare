package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.model.Paciente;
import com.esperanza.hopecare.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacienteDAO {
    private static final Logger LOGGER = Logger.getLogger(PacienteDAO.class.getName());

    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT p.id_paciente, p.id_persona, p.historia_clinica, p.alergias, p.grupo_sanguineo, p.contacto_emergencia, p.activo, "
                   + "per.nombre, per.apellido, per.documento_identidad, per.fecha_nacimiento, per.telefono, per.email, per.direccion, per.genero "
                   + "FROM paciente p "
                   + "JOIN persona per ON p.id_persona = per.id_persona";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
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
                p.setActivo(rs.getInt("activo"));
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
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al listar todos los pacientes", e); 
        }
        return lista;
    }

    public List<Paciente> listarActivos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT p.id_paciente, p.id_persona, p.historia_clinica, p.alergias, p.grupo_sanguineo, p.contacto_emergencia, p.activo, "
                   + "per.nombre, per.apellido, per.documento_identidad, per.fecha_nacimiento, per.telefono, per.email, per.direccion, per.genero "
                   + "FROM paciente p "
                   + "JOIN persona per ON p.id_persona = per.id_persona "
                   + "WHERE p.activo = 1";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
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
                p.setActivo(rs.getInt("activo"));
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
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al listar pacientes activos", e); 
        }
        return lista;
    }

    public boolean existeDocumento(String documento, int excluirIdPersona) {
        String sql = "SELECT COUNT(*) FROM persona WHERE documento_identidad = ? AND id_persona != ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.setInt(2, excluirIdPersona);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al verificar existencia de documento", e); 
        }
        return false;
    }

    public boolean existeHistoriaClinica(String historiaClinica, int excluirIdPaciente) {
        String sql = "SELECT COUNT(*) FROM paciente WHERE historia_clinica = ? AND id_paciente != ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, historiaClinica);
            ps.setInt(2, excluirIdPaciente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al verificar existencia de historia clínica", e); 
        }
        return false;
    }

    public int obtenerIdPacientePorIdPersona(int idPersona) {
        String sql = "SELECT id_paciente FROM paciente WHERE id_persona = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_paciente");
            }
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al obtener ID paciente por ID persona", e);
        }
        return -1;
    }

    public Paciente obtenerPorIdPersona(int idPersona) {
        String sql = "SELECT p.id_paciente, p.id_persona, p.historia_clinica, p.alergias, p.grupo_sanguineo, p.contacto_emergencia, p.activo, "
                   + "per.nombre, per.apellido, per.documento_identidad, per.fecha_nacimiento, per.telefono, per.email, per.direccion, per.genero "
                   + "FROM paciente p "
                   + "JOIN persona per ON p.id_persona = per.id_persona "
                   + "WHERE p.id_persona = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Paciente p = new Paciente();
                p.setIdPaciente(rs.getInt("id_paciente"));
                p.setIdPersona(rs.getInt("id_persona"));
                p.setHistoriaClinica(rs.getString("historia_clinica"));
                p.setAlergias(rs.getString("alergias"));
                p.setGrupoSanguineo(rs.getString("grupo_sanguineo"));
                p.setContactoEmergencia(rs.getString("contacto_emergencia"));
                p.setActivo(rs.getInt("activo"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setDocumentoIdentidad(rs.getString("documento_identidad"));
                p.setFechaNacimiento(rs.getString("fecha_nacimiento"));
                p.setTelefono(rs.getString("telefono"));
                p.setEmail(rs.getString("email"));
                p.setDireccion(rs.getString("direccion"));
                p.setGenero(rs.getString("genero"));
                return p;
            }
        } catch (SQLException e) { 
            LOGGER.log(Level.SEVERE, "Error al obtener paciente por ID persona", e);
        }
        return null;
    }

    private void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    public boolean insertar(Paciente p) {
        String sqlPersona = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPaciente = "INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia, activo) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getClinicaConnection();
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
                psPac.setInt(6, p.getActivo());
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
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error en rollback de inserción", ex); }
            }
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                LOGGER.log(Level.INFO, "Intento de inserción con datos duplicados: {0}", e.getMessage());
            } else {
                LOGGER.log(Level.SEVERE, "Error al insertar paciente", e);
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error al cerrar conexión en inserción", ex); }
            }
        }
    }

    public boolean actualizar(Paciente p) {
        String sqlPersona = "UPDATE persona SET nombre = ?, apellido = ?, documento_identidad = ?, fecha_nacimiento = ?, telefono = ?, email = ?, direccion = ?, genero = ? WHERE id_persona = ?";
        String sqlPaciente = "UPDATE paciente SET historia_clinica = ?, alergias = ?, grupo_sanguineo = ?, contacto_emergencia = ?, activo = ? WHERE id_paciente = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getClinicaConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement psP = conn.prepareStatement(sqlPersona)) {
                psP.setString(1, p.getNombre().trim());
                psP.setString(2, p.getApellido().trim());
                psP.setString(3, p.getDocumentoIdentidad().trim());
                setStringOrNull(psP, 4, p.getFechaNacimiento());
                setStringOrNull(psP, 5, p.getTelefono());
                setStringOrNull(psP, 6, p.getEmail());
                setStringOrNull(psP, 7, p.getDireccion());
                setStringOrNull(psP, 8, p.getGenero());
                psP.setInt(9, p.getIdPersona());
                psP.executeUpdate();
            }
            
            try (PreparedStatement psPac = conn.prepareStatement(sqlPaciente)) {
                psPac.setString(1, p.getHistoriaClinica().trim());
                setStringOrNull(psPac, 2, p.getAlergias());
                setStringOrNull(psPac, 3, p.getGrupoSanguineo());
                setStringOrNull(psPac, 4, p.getContactoEmergencia());
                psPac.setInt(5, p.getActivo());
                psPac.setInt(6, p.getIdPaciente());
                psPac.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error en rollback de actualización", ex); }
            }
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                LOGGER.log(Level.INFO, "Intento de actualización con datos duplicados: {0}", e.getMessage());
            } else {
                LOGGER.log(Level.SEVERE, "Error al actualizar paciente", e);
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error al cerrar conexión en actualización", ex); }
            }
        }
    }

    public boolean eliminar(int idPaciente) throws SQLException {
        String sqlGetPersona = "SELECT id_persona FROM paciente WHERE id_paciente = ?";
        String sqlPaciente = "DELETE FROM paciente WHERE id_paciente = ?";
        String sqlPersona = "DELETE FROM persona WHERE id_persona = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getClinicaConnection();
            conn.setAutoCommit(false);
            
            int idPersona = -1;
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetPersona)) {
                psGet.setInt(1, idPaciente);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        idPersona = rs.getInt("id_persona");
                    }
                }
            }
            
            if (idPersona == -1) {
                return false;
            }
            
            try (PreparedStatement psPac = conn.prepareStatement(sqlPaciente)) {
                psPac.setInt(1, idPaciente);
                psPac.executeUpdate();
            }
            
            try (PreparedStatement psP = conn.prepareStatement(sqlPersona)) {
                psP.setInt(1, idPersona);
                psP.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error en rollback de eliminación", ex); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error al cerrar conexión en eliminación", ex); }
            }
        }
    }

    public boolean darDeAlta(int idPaciente) {
        String sql = "UPDATE paciente SET activo = 0 WHERE id_paciente = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al dar de alta (baja lógica) paciente", e);
            return false;
        }
    }

    public boolean reactivar(int idPaciente) {
        String sql = "UPDATE paciente SET activo = 1 WHERE id_paciente = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al reactivar paciente", e);
            return false;
        }
    }
}
