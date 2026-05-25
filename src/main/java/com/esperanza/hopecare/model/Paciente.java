package com.esperanza.hopecare.model;

import com.esperanza.hopecare.model.Persona;

public class Paciente extends Persona {
    private int idPaciente;
    private String historiaClinica;
    private String alergias;
    private String grupoSanguineo;
    private String contactoEmergencia;
    private int activo = 1;

    public Paciente() {}

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public String getHistoriaClinica() { return historiaClinica; }
    public void setHistoriaClinica(String historiaClinica) { this.historiaClinica = historiaClinica; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }

    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }

    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }

    public String getEstado() { return activo == 1 ? "Activo" : "De alta"; }

    @Override
    public String toString() {
        return (getNombre() != null ? getNombre() + " " + getApellido() : "Paciente #" + idPaciente)
               + (getDocumentoIdentidad() != null ? " (" + getDocumentoIdentidad() + ")" : "");
    }
}
