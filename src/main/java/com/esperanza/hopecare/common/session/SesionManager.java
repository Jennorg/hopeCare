package com.esperanza.hopecare.common.session;

public class SesionManager {

    private static SesionManager instance;

    private String nombreUsuario;
    private String nombrePersona;
    private String rol;
    private int idUsuario;
    private int idPersona;

    private SesionManager() {}

    public static SesionManager getInstance() {
        if (instance == null) {
            instance = new SesionManager();
        }
        return instance;
    }

    public void iniciarSesion(String nombreUsuario, String nombrePersona, String rol, int idUsuario, int idPersona) {
        this.nombreUsuario = nombreUsuario;
        this.nombrePersona = nombrePersona;
        this.rol = rol;
        this.idUsuario = idUsuario;
        this.idPersona = idPersona;
    }

    public void cerrarSesion() {
        this.nombreUsuario = null;
        this.nombrePersona = null;
        this.rol = null;
        this.idUsuario = -1;
        this.idPersona = -1;
    }

    public boolean haySesionActiva() {
        return nombreUsuario != null;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombrePersona() { return nombrePersona; }
    public String getRol() { return rol; }
    public int getIdUsuario() { return idUsuario; }
    public int getIdPersona() { return idPersona; }
}
