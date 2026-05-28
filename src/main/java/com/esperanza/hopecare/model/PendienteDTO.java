package com.esperanza.hopecare.model;

public class PendienteDTO {
    private int idPaciente;
    private int idReferencia;
    private String pacienteNombre;
    private String concepto;
    private double monto;
    private String fecha;
    private String tipoReferencia;

    public PendienteDTO(int idPaciente, int idReferencia, String pacienteNombre, String concepto, double monto, String fecha, String tipoReferencia) {
        this.idPaciente = idPaciente;
        this.idReferencia = idReferencia;
        this.pacienteNombre = pacienteNombre;
        this.concepto = concepto;
        this.monto = monto;
        this.fecha = fecha;
        this.tipoReferencia = tipoReferencia;
    }

    public int getIdPaciente() { return idPaciente; }
    public int getIdReferencia() { return idReferencia; }
    public String getPacienteNombre() { return pacienteNombre; }
    public String getConcepto() { return concepto; }
    public double getMonto() { return monto; }
    public String getFecha() { return fecha; }
    public String getTipoReferencia() { return tipoReferencia; }
}
