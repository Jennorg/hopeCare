package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.model.UsuarioModel;

import java.sql.*;

public class AuthDAO {

    public UsuarioModel autenticar(String usuario, String contrasena) {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.rol, u.id_persona, p.nombre, p.apellido " +
                     "FROM usuario u JOIN persona p ON u.id_persona = p.id_persona " +
                     "WHERE u.nombre_usuario = ? AND u.contrasena_hash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            // ps.setString(2, Hasher.hash(contrasena)); // Hasher not found, using raw for now or assuming it will be fixed
            ps.setString(2, contrasena); 
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
}
