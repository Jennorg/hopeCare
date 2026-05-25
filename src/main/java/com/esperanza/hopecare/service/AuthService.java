package com.esperanza.hopecare.service;

import com.esperanza.hopecare.dao.AuthDAO;
import com.esperanza.hopecare.model.LoginDTO;
import com.esperanza.hopecare.model.RegistroDTO;
import com.esperanza.hopecare.model.Persona;
import com.esperanza.hopecare.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            dto.setMensaje("Usuario y contraseña son requeridos.");
            return dto;
        }
        var model = authDAO.autenticar(usuario, contrasena);
        if (model != null) {
            dto.setExitoso(true);
            dto.setNombreRol(model.getNombreRol());
            dto.setRol(model.getRol());
            dto.setIdPersona(model.getIdPersona());
            dto.setMensaje("Inicio de sesión exitoso.");
        } else {
            dto.setExitoso(false);
            dto.setMensaje("Usuario o contraseña incorrectos.");
        }
        return dto;
    }

    public boolean usuarioExiste(String usuario) { return authDAO.usuarioExiste(usuario); }
    public boolean emailExiste(String email) { return email != null && !email.isEmpty() && authDAO.emailExiste(email); }
    public boolean documentoExiste(String documento) { return documento != null && !documento.isEmpty() && authDAO.documentoExiste(documento); }
    public boolean registroMedicoExiste(String registroMedico) { return registroMedico != null && !registroMedico.isEmpty() && authDAO.registroMedicoExiste(registroMedico); }

    public String registrar(RegistroDTO dto) {
        if (authDAO.usuarioExiste(dto.getNombreUsuario())) return "El nombre de usuario ya está en uso.";
        if (dto.getDocumento() != null && !dto.getDocumento().isEmpty() && authDAO.documentoExiste(dto.getDocumento())) return "Esta cédula ya está registrada.";
        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && authDAO.emailExiste(dto.getEmail())) return "Este correo ya está registrado.";
        if ("MEDICO".equals(dto.getRol()) && authDAO.registroMedicoExiste(dto.getRegistroMedico())) return "Este registro médico ya está registrado.";

        try (Connection connClinica = DatabaseConnection.getClinicaConnection();
             Connection connAuth = DatabaseConnection.getAuthConnection();
             Connection connCitas = DatabaseConnection.getCitasConnection()) {
            
            connClinica.setAutoCommit(false);
            connAuth.setAutoCommit(false);
            connCitas.setAutoCommit(false);
            
            try {
                Persona persona = new Persona();
                persona.setNombre(dto.getNombre());
                persona.setApellido(dto.getApellido());
                persona.setDocumentoIdentidad(dto.getDocumento());
                persona.setFechaNacimiento(dto.getFechaNacimiento() != null ? dto.getFechaNacimiento().toString() : null);
                persona.setTelefono(dto.getTelefono());
                persona.setGenero(dto.getGenero());
                persona.setEmail(dto.getEmail());
                persona.setDireccion(dto.getDireccion());

                int idPersona = authDAO.insertarPersona(connClinica, persona);
                
                String rolBD = dto.getRol();
                if ("ADMINISTRADOR".equals(rolBD)) rolBD = "ADMIN";
                
                authDAO.insertarUsuario(connAuth, dto.getNombreUsuario(), dto.getContrasena(), rolBD, idPersona);

                if ("PACIENTE".equals(dto.getRol())) {
                    authDAO.insertarPaciente(connClinica, idPersona, "HC-" + System.currentTimeMillis());
                } else if ("MEDICO".equals(dto.getRol())) {
                    int idEspecialidad = authDAO.obtenerIdEspecialidad(connClinica, dto.getEspecialidad());
                    authDAO.insertarMedico(connClinica, idPersona, idEspecialidad, dto.getRegistroMedico(), dto.getTarifa());
                    
                    // Obtener el ID del médico recién insertado
                    int idMedico = -1;
                    try (PreparedStatement psId = connClinica.prepareStatement("SELECT id_medico FROM medico WHERE id_persona = ?")) {
                        psId.setInt(1, idPersona);
                        ResultSet rs = psId.executeQuery();
                        if (rs.next()) idMedico = rs.getInt(1);
                    }
                    
                    if (idMedico > 0) {
                        String sqlH = "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement psH = connCitas.prepareStatement(sqlH)) {
                            for (int dia = 1; dia <= 7; dia++) {
                                psH.setInt(1, idMedico);
                                psH.setInt(2, dia);
                                psH.setString(3, "08:00");
                                psH.setString(4, "12:00");
                                psH.setInt(5, 30);
                                psH.addBatch();
                            }
                            psH.executeBatch();
                        }
                    }
                }

                connClinica.commit();
                connAuth.commit();
                connCitas.commit();
                return null;
            } catch (SQLException e) {
                connClinica.rollback();
                connAuth.rollback();
                connCitas.rollback();
                e.printStackTrace();
                return "Error al registrar: " + e.getMessage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error de conexión: " + e.getMessage();
        }
    }
}
