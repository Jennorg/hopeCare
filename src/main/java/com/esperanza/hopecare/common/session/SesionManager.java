package com.esperanza.hopecare.common.session;

public class SesionManager {
    private static SesionManager instance;
    private String nombreUsuario;
    private String nombreRol;
    private int idUsuario;

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
    }

    public void cerrarSesion() {
        this.idUsuario = 0;
        this.nombreUsuario = null;
        this.nombreRol = null;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombreRol() { return nombreRol; }
    public int getIdUsuario() { return idUsuario; }
    
    public boolean isAdmin() { return "ADMIN".equals(nombreRol); }
    public boolean isRecepcionista() { return "RECEPCIONISTA".equals(nombreRol); }
    public boolean isMedico() { return "MEDICO".equals(nombreRol); }
}
