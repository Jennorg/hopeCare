package com.esperanza.hopecare.modules.Auth.dao;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.common.utils.Hasher;
import com.esperanza.hopecare.modules.Auth.model.PersonaModel;
import com.esperanza.hopecare.modules.Auth.model.UsuarioModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthDAO {

    public UsuarioModel autenticar(String usuario, String contrasena) {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, r.nombre_rol AS rol, u.id_persona " +
                     "FROM usuario u JOIN rol r ON u.id_rol = r.id_rol " +
                     "WHERE u.nombre_usuario = ? AND u.contrasena_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, Hasher.hash(contrasena));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UsuarioModel model = new UsuarioModel();
                model.setIdUsuario(rs.getInt("id_usuario"));
                model.setNombreUsuario(rs.getString("nombre_usuario"));
                model.setNombreRol(rs.getString("rol"));
                model.setIdPersona(rs.getInt("id_persona"));
                return model;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean usuarioExiste(String nombreUsuario) {
        String sql = "SELECT 1 FROM usuario WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean emailExiste(String email) {
        String sql = "SELECT 1 FROM persona WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean documentoExiste(String documento) {
        String sql = "SELECT 1 FROM persona WHERE documento_identidad = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean registroMedicoExiste(String registroMedico) {
        String sql = "SELECT 1 FROM medico WHERE registro_medico = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registroMedico);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int insertarUsuario(Connection conn, String username, String password, String rolNombre, int idPersona) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_usuario, contrasena_hash, id_rol, id_persona) SELECT ?, ?, id_rol, ? FROM rol WHERE nombre_rol = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username); ps.setString(2, Hasher.hash(password)); ps.setInt(3, idPersona); ps.setString(4, rolNombre);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            try (PreparedStatement ps2 = conn.prepareStatement("SELECT id_usuario FROM usuario WHERE nombre_usuario = ?")) {
                ps2.setString(1, username); ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) return rs2.getInt(1);
            }
            throw new SQLException("No se pudo obtener id_usuario para " + username);
        }
    }

    public int insertarPersona(Connection conn, PersonaModel persona) throws SQLException {
        String sql = "INSERT INTO persona (nombre, apellido, documento_identidad, fecha_nacimiento, telefono, genero, email, direccion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, persona.getNombre()); ps.setString(2, persona.getApellido());
            ps.setString(3, emptyToNull(persona.getDocumentoIdentidad())); ps.setString(4, persona.getFechaNacimiento());
            ps.setString(5, emptyToNull(persona.getTelefono())); ps.setString(6, persona.getGenero());
            ps.setString(7, emptyToNull(persona.getEmail())); ps.setString(8, emptyToNull(persona.getDireccion()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo crear la persona");
        }
    }

    public void insertarMedico(Connection conn, int idPersona, int idEspecialidad, String registroMedico) throws SQLException {
        String sql = "INSERT INTO medico (id_persona, id_especialidad, registro_medico, precio_consulta) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona); ps.setInt(2, idEspecialidad); ps.setString(3, registroMedico); ps.setDouble(4, 0.0);
            ps.executeUpdate();
        }
    }

    public void insertarPaciente(Connection conn, int idPersona, String historiaClinica) throws SQLException {
        String sql = "INSERT INTO paciente (id_persona, historia_clinica) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona); ps.setString(2, historiaClinica); ps.executeUpdate();
        }
    }

    public int obtenerIdEspecialidad(Connection conn, String nombre) throws SQLException {
        String sql = "SELECT id_especialidad FROM especialidad WHERE nombre_especialidad = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            try (PreparedStatement ps2 = conn.prepareStatement("INSERT INTO especialidad (nombre_especialidad) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps2.setString(1, nombre); ps2.executeUpdate();
                ResultSet rs2 = ps2.getGeneratedKeys(); if (rs2.next()) return rs2.getInt(1);
            }
            throw new SQLException("No se pudo crear/obtener la especialidad");
        }
    }

    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}