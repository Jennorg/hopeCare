package com.esperanza.hopecare.model;

import com.esperanza.hopecare.model.Persona;

public class Medico extends Persona {
    private int idMedico;
    private int idEspecialidad;
    private String registroMedico;
    private String nombreEspecialidad;
    private double precioConsulta;
    private int activo;

    public Medico() {}

    public Medico(String documentoIdentidad, int idEspecialidad, String registroMedico) {
        super(documentoIdentidad);
        this.idEspecialidad = idEspecialidad;
        this.registroMedico = registroMedico;
    }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

    public int getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(int idEspecialidad) { this.idEspecialidad = idEspecialidad; }

    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String registroMedico) { this.registroMedico = registroMedico; }

    public String getNombreEspecialidad() { return nombreEspecialidad; }
    public void setNombreEspecialidad(String nombreEspecialidad) { this.nombreEspecialidad = nombreEspecialidad; }

    public double getPrecioConsulta() { return precioConsulta; }
    public void setPrecioConsulta(double precioConsulta) { this.precioConsulta = precioConsulta; }

    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }

    @Override
    public String toString() {
        return (getNombre() != null ? getNombre() + " " + getApellido() : "Médico #" + idMedico)
               + (nombreEspecialidad != null ? " - " + nombreEspecialidad : "");
    }
}
