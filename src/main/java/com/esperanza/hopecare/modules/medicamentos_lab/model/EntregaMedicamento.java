package com.esperanza.hopecare.modules.medicamentos_lab.model;

import java.time.LocalDateTime;

public class EntregaMedicamento {
    private int idEntrega;
    private int idPaciente;
    private int idMedicamento;
    private int cantidadEntregada;
    private boolean presenteReceta;
    private int entregadoPor;
    private LocalDateTime fechaEntrega;
    private boolean facturado;

    private String pacienteNombre;
    private String pacienteApellido;
    private String medicamentoNombre;

    public EntregaMedicamento() {}

    public EntregaMedicamento(int idPaciente, int idMedicamento, int cantidadEntregada, boolean presenteReceta, int entregadoPor) {
        this.idPaciente = idPaciente;
        this.idMedicamento = idMedicamento;
        this.cantidadEntregada = cantidadEntregada;
        this.presenteReceta = presenteReceta;
        this.entregadoPor = entregadoPor;
        this.fechaEntrega = LocalDateTime.now();
        this.facturado = false;
    }

    public int getIdEntrega() { return idEntrega; }
    public void setIdEntrega(int idEntrega) { this.idEntrega = idEntrega; }
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
    public int getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(int idMedicamento) { this.idMedicamento = idMedicamento; }
    public int getCantidadEntregada() { return cantidadEntregada; }
    public void setCantidadEntregada(int cantidadEntregada) { this.cantidadEntregada = cantidadEntregada; }
    public boolean isPresenteReceta() { return presenteReceta; }
    public void setPresenteReceta(boolean presenteReceta) { this.presenteReceta = presenteReceta; }
    public int getEntregadoPor() { return entregadoPor; }
    public void setEntregadoPor(int entregadoPor) { this.entregadoPor = entregadoPor; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
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
    public String getMedicamentoNombre() { return medicamentoNombre; }
    public void setMedicamentoNombre(String medicamentoNombre) { this.medicamentoNombre = medicamentoNombre; }
}