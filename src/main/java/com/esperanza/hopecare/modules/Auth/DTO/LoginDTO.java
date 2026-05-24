package com.esperanza.hopecare.modules.Auth.dto;

public class LoginDTO {
    private String nombreUsuario;
    private String contrasena;
    private boolean exitoso;
    private String nombreRol;
    private String rol;
    private int idPersona = -1;
    private String mensaje;

    public LoginDTO() {}
    public LoginDTO(String nombreUsuario, String contrasena) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
    }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String v) { this.nombreUsuario = v; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String v) { this.contrasena = v; }
    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean v) { this.exitoso = v; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String v) { this.nombreRol = v; }
    public String getRol() { return rol; }
    public void setRol(String v) { this.rol = v; }
    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int v) { this.idPersona = v; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String v) { this.mensaje = v; }
}
