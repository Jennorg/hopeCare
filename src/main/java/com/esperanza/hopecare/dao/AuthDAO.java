package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.util.Hasher;
import com.esperanza.hopecare.model.UsuarioModel;
import com.esperanza.hopecare.model.Persona;

import java.sql.*;

public class AuthDAO {

    public UsuarioModel autenticar(String usuario, String contrasena) {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.rol, u.id_persona, p.nombre, p.apellido " +
                     "FROM usuario u JOIN clinica.persona p ON u.id_persona = p.id_persona " +
                     "WHERE u.nombre_usuario = ? AND (u.contrasena_hash = ? OR u.contrasena = ?)";
        try (Connection conn = DatabaseConnection.getAuthWithClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String hashed = Hasher.hash(contrasena);
            ps.setString(1, usuario);
            ps.setString(2, hashed);
            ps.setString(3, contrasena); 
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UsuarioModel model = new UsuarioModel();
                model.setIdUsuario(rs.getInt("id_usuario"));
                model.setNombreUsuario(rs.getString("nombre_usuario"));
                model.setRol(rs.getString("rol"));
                model.setIdPersona(rs.getInt("id_persona"));
                model.setNombreRol(rs.getString("nombre") + " " + rs.getString("apellido"));
                return model;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean usuarioExiste(String usuario) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM persona WHERE email = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean documentoExiste(String documento) {
        String sql = "SELECT COUNT(*) FROM persona WHERE documento_identidad = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean registroMedicoExiste(String registroMedico) {
        String sql = "SELECT COUNT(*) FROM medico WHERE registro_medico = ?";
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registroMedico);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int insertarPersona(Connection conn, Persona p) throws SQLException {
        String sql = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, email, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getDocumentoIdentidad());
            ps.setString(4, p.getFechaNacimiento());
            ps.setString(5, p.getTelefono());
            ps.setString(6, p.getEmail());
            ps.setString(7, p.getDireccion());
            ps.setString(8, p.getGenero());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener el ID de persona generado.");
        }
    }

    public int insertarUsuario(Connection conn, String user, String pass, String rol, int idPersona) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, contrasena, contrasena_hash, rol, id_persona) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, Hasher.hash(pass));
            ps.setString(4, rol);
            ps.setInt(5, idPersona);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener el ID de usuario generado.");
        }
    }

    public void insertarPaciente(Connection conn, int idPersona, String historiaClinica) throws SQLException {
        String sql = "INSERT INTO paciente (id_persona, historia_clinica) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ps.setString(2, historiaClinica);
            ps.executeUpdate();
        }
    }
}
