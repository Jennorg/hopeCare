package com.esperanza.hopecare.util;

public class SesionManager {
    private static SesionManager instance;
    private String nombreUsuario;
    private String nombrePersona;
    private String rol;
    private String nombreRol;
    private int idUsuario;
    private int idPersona;

    private SesionManager() {}

    public static SesionManager getInstance() {
        if (instance == null) {
            instance = new SesionManager();
        }
        return instance;
    }

    public void iniciarSesion(int idUsuario, String nombreUsuario, String nombreRol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombreRol = nombreRol;
        this.rol = nombreRol; // Support both naming conventions
    }

    public void iniciarSesion(String nombreUsuario, String nombrePersona, String rol, int idUsuario, int idPersona) {
        this.nombreUsuario = nombreUsuario;
        this.nombrePersona = nombrePersona;
        this.rol = rol;
        this.nombreRol = rol; // Support both naming conventions
        this.idUsuario = idUsuario;
        this.idPersona = idPersona;
    }

    public void cerrarSesion() {
        this.idUsuario = 0;
        this.nombreUsuario = null;
        this.nombrePersona = null;
        this.rol = null;
        this.nombreRol = null;
        this.idUsuario = -1;
        this.idPersona = -1;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombrePersona() { return nombrePersona; }
    public String getRol() { return rol; }
    public String getNombreRol() { return nombreRol; }
    public int getIdUsuario() { return idUsuario; }
    public int getIdPersona() { return idPersona; }
    
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(nombreRol) || "ADMIN".equalsIgnoreCase(rol); }
    public boolean isRecepcionista() { return "RECEPCIONISTA".equalsIgnoreCase(nombreRol) || "RECEPCIONISTA".equalsIgnoreCase(rol); }
    public boolean isMedico() { return "MEDICO".equalsIgnoreCase(nombreRol) || "MEDICO".equalsIgnoreCase(rol); }
}
