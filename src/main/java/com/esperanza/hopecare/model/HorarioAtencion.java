package com.esperanza.hopecare.model;

import java.time.LocalTime;

public class HorarioAtencion {
    private int idMedico;
    private int diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int intervaloMinutos;
    private int activo;

    public HorarioAtencion() {}

    // Getters and Setters
    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
    public int getDiaSemana() { return diaSemana; }
    public void setDiaSemana(int diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public int getIntervaloMinutos() { return intervaloMinutos; }
    public void setIntervaloMinutos(int intervaloMinutos) { this.intervaloMinutos = intervaloMinutos; }
    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }
}
