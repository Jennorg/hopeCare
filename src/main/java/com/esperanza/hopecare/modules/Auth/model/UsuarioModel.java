package com.esperanza.hopecare.modules.Auth.model;

public class UsuarioModel {
    private int idUsuario;
    private int idPersona;
    private String nombreUsuario;
    private String contrasenaHash;
    private int idRol;
    private String nombreRol;
    private String fechaCreacion;

    public UsuarioModel() {}

    public UsuarioModel(int idUsuario, String nombreUsuario, String nombreRol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombreRol = nombreRol;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
