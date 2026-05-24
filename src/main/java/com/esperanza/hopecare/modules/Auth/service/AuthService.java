package com.esperanza.hopecare.modules.Auth.service;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.Auth.dao.AuthDAO;
import com.esperanza.hopecare.modules.Auth.dto.LoginDTO;
import com.esperanza.hopecare.modules.Auth.dto.RegistroDTO;
import com.esperanza.hopecare.modules.Auth.model.PersonaModel;
import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {

    private final AuthDAO authDAO;

    public AuthService() {
        this.authDAO = new AuthDAO();
    }

    public LoginDTO login(String usuario, String contrasena) {
        LoginDTO dto = new LoginDTO(usuario, contrasena);
        if (usuario == null || usuario.trim().isEmpty() || contrasena == null || contrasena.isEmpty()) {
            dto.setExitoso(false);
            dto.setMensaje("Usuario y contrasena son requeridos.");
            return dto;
        }
        var model = authDAO.autenticar(usuario, contrasena);
        if (model != null) {
            dto.setExitoso(true);
            dto.setNombreRol(model.getNombreRol());
            dto.setRol(model.getRol());
            dto.setIdPersona(model.getIdPersona());
            dto.setMensaje("Inicio de sesion exitoso.");
        } else {
            dto.setExitoso(false);
            dto.setMensaje("Usuario o contrasena incorrectos.");
        }
        return dto;
    }
    public boolean usuarioExiste(String usuario) { return authDAO.usuarioExiste(usuario); }
    public boolean emailExiste(String email) { return email != null && !email.isEmpty() && authDAO.emailExiste(email); }
    public boolean documentoExiste(String documento) { return documento != null && !documento.isEmpty() && authDAO.documentoExiste(documento); }
    public boolean registroMedicoExiste(String registroMedico) { return registroMedico != null && !registroMedico.isEmpty() && authDAO.registroMedicoExiste(registroMedico); }

    public String registrar(RegistroDTO dto) {
        if (authDAO.usuarioExiste(dto.getNombreUsuario())) return "El nombre de usuario ya esta en uso.";
        if (dto.getDocumento() != null && !dto.getDocumento().isEmpty() && authDAO.documentoExiste(dto.getDocumento())) return "Esta cedula ya esta registrada.";
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && authDAO.emailExiste(dto.getEmail())) return "Este correo ya esta registrado.";
        if ("MEDICO".equals(dto.getRol()) && authDAO.registroMedicoExiste(dto.getRegistroMedico())) return "Este registro medico ya esta registrado.";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            PersonaModel persona = new PersonaModel();
            persona.setNombre(dto.getNombre());
            persona.setApellido(dto.getApellido());
            persona.setDocumentoIdentidad(dto.getDocumento());
            persona.setFechaNacimiento(dto.getFechaNacimiento() != null ? dto.getFechaNacimiento().toString() : null);
            persona.setTelefono(dto.getTelefono());
            persona.setGenero(dto.getGenero());
            persona.setEmail(dto.getEmail());
            persona.setDireccion(dto.getDireccion());
            int idPersona = authDAO.insertarPersona(conn, persona);
            String rolBD = dto.getRol();
            if ("ADMINISTRADOR".equals(rolBD)) rolBD = "ADMIN";
            int idUsuario = authDAO.insertarUsuario(conn, dto.getNombreUsuario(), dto.getContrasena(), rolBD, idPersona);
            if ("PACIENTE".equals(dto.getRol())) {
                authDAO.insertarPaciente(conn, idPersona, "HC-" + System.currentTimeMillis());
            }
            conn.commit();
            return null;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            e.printStackTrace();
            return "Error al registrar: " + e.getMessage();
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }
}
            conn.commit();
            return null;
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            e.printStackTrace();
            return "Error al registrar: " + e.getMessage();
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }
}
