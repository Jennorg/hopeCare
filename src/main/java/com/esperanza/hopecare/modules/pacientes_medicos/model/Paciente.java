package com.esperanza.hopecare.modules.pacientes_medicos.model;

import com.esperanza.hopecare.common.model.Persona;

public class Paciente extends Persona {
    private int idPaciente;
    private String historiaClinica;
    private String alergias;
    private String grupoSanguineo;
    private String contactoEmergencia;

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
}
