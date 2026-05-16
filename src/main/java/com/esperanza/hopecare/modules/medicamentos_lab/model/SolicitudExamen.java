package com.esperanza.hopecare.modules.medicamentos_lab.model;

import java.time.LocalDateTime;

public class SolicitudExamen {
    private int idSolicitud;
    private int idPaciente;
    private int idExamen;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String resultadoTexto;
    private byte[] resultadoArchivo;
    private int realizadoPor;
    private boolean facturado;

    private String pacienteNombre;
    private String pacienteApellido;
    private String examenNombre;

    public SolicitudExamen() {}

    public SolicitudExamen(int idPaciente, int idExamen) {
        this.idPaciente = idPaciente;
        this.idExamen = idExamen;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.facturado = false;
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
    public int getIdExamen() { return idExamen; }
    public void setIdExamen(int idExamen) { this.idExamen = idExamen; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getResultadoTexto() { return resultadoTexto; }
    public void setResultadoTexto(String resultadoTexto) { this.resultadoTexto = resultadoTexto; }
    public byte[] getResultadoArchivo() { return resultadoArchivo; }
    public void setResultadoArchivo(byte[] resultadoArchivo) { this.resultadoArchivo = resultadoArchivo; }
    public int getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(int realizadoPor) { this.realizadoPor = realizadoPor; }
    public boolean isFacturado() { return facturado; }
    public void setFacturado(boolean facturado) { this.facturado = facturado; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public String getPacienteApellido() { return pacienteApellido; }
    public void setPacienteApellido(String pacienteApellido) { this.pacienteApellido = pacienteApellido; }
    public String getPacienteNombreCompleto() {
        if (pacienteNombre != null && pacienteApellido != null) {
            return pacienteNombre + " " + pacienteApellido;
        }
        return pacienteNombre != null ? pacienteNombre : "";
    }
    public String getExamenNombre() { return examenNombre; }
    public void setExamenNombre(String examenNombre) { this.examenNombre = examenNombre; }
}