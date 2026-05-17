/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.esperanza.hopecare.modules.pacientes_medicos.model;

import com.esperanza.hopecare.common.model.Persona;

/**
 *
 * @author Jenfer
 */


public class Medico extends Persona {
    private int idMedico;
    private int idEspecialidad;
    private String registroMedico;
    private String nombreEspecialidad;
    private double precioConsulta;

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

    @Override
    public String toString() {
        return (getNombre() != null ? getNombre() + " " + getApellido() : "Médico #" + idMedico)
               + (nombreEspecialidad != null ? " - " + nombreEspecialidad : "");
    }
}