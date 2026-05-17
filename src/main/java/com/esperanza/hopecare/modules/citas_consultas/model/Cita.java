package com.esperanza.hopecare.modules.citas_consultas.model;

import java.time.LocalDateTime;

public class Cita {
    private int idCita;
    private int idPaciente;
    private int idMedico;
    private LocalDateTime fechaHora;
    private String estado;
    private String motivo;
    private int creadaPor;
    private LocalDateTime fechaCreacion;
    private String pacienteNombre;
    private String pacienteDocumento;
    private String medicoNombre;
    private double precio;

    public Cita() {}

    public Cita(int idPaciente, int idMedico, LocalDateTime fechaHora, String estado) {
        this.idPaciente = idPaciente;
        this.idMedico = idMedico;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public int getCreadaPor() { return creadaPor; }
    public void setCreadaPor(int creadaPor) { this.creadaPor = creadaPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    public String getPacienteDocumento() { return pacienteDocumento; }
    public void setPacienteDocumento(String pacienteDocumento) { this.pacienteDocumento = pacienteDocumento; }
    public String getMedicoNombre() { return medicoNombre; }
    public void setMedicoNombre(String medicoNombre) { this.medicoNombre = medicoNombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}
