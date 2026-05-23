package com.esperanza.hopecare.model;

import java.time.LocalDateTime;

public class Consulta {
    private int idConsulta;
    private int idCita;
    private String diagnostico;
    private String sintomas;
    private String tratamiento;
    private String notasMedicas;
    private LocalDateTime fechaConsulta;
    private double precio;

    public Consulta() {}

    public Consulta(int idCita, String diagnostico, String sintomas, String tratamiento, double precio) {
        this.idCita = idCita;
        this.diagnostico = diagnostico;
        this.sintomas = sintomas;
        this.tratamiento = tratamiento;
        this.precio = precio;
    }

    public int getIdConsulta() { return idConsulta; }
    public void setIdConsulta(int idConsulta) { this.idConsulta = idConsulta; }
    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getNotasMedicas() { return notasMedicas; }
    public void setNotasMedicas(String notasMedicas) { this.notasMedicas = notasMedicas; }
    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDateTime fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}
