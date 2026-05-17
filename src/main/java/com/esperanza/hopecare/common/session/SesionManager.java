package com.esperanza.hopecare.common.session;

public class SesionManager {

    private static SesionManager instance;

    private String nombreUsuario;
    private String nombreRol;

    private SesionManager() {}

    public static SesionManager getInstance() {
        if (instance == null) {
            instance = new SesionManager();
        }
        return instance;
    }

    public void iniciarSesion(String nombreUsuario, String nombreRol) {
        this.nombreUsuario = nombreUsuario;
        this.nombreRol = nombreRol;
    }

    public void cerrarSesion() {
        this.nombreUsuario = null;
        this.nombreRol = null;
    }

    public boolean haySesionActiva() {
        return nombreUsuario != null;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombreRol() { return nombreRol; }
}
