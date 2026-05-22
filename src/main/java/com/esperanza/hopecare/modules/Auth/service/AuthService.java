package com.esperanza.hopecare.modules.Auth.service;

import com.esperanza.hopecare.modules.Auth.dao.AuthDAO;
import com.esperanza.hopecare.modules.Auth.dto.LoginDTO;

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
}
