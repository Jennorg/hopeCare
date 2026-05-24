package com.esperanza.hopecare.model;

import java.util.Objects;

/**
 * Data Transfer Object inmutable para los detalles de factura.
 * tipoReferencia puede ser: "CONSULTA", "EXAMEN", "MEDICAMENTO".
 */
public final class DetalleFacturaDTO {
    private final String concepto;
    private final int idReferencia;
    private final String tipoReferencia;
    private final double monto;

    public DetalleFacturaDTO(String concepto, int idReferencia, String tipoReferencia, double monto) {
        this.concepto = concepto;
        this.idReferencia = idReferencia;
        this.tipoReferencia = tipoReferencia;
        this.monto = monto;
    }

    public String getConcepto() { return concepto; }
    public int getIdReferencia() { return idReferencia; }
    public String getTipoReferencia() { return tipoReferencia; }
    public double getMonto() { return monto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleFacturaDTO that = (DetalleFacturaDTO) o;
        return idReferencia == that.idReferencia &&
               Double.compare(that.monto, monto) == 0 &&
               Objects.equals(concepto, that.concepto) &&
               Objects.equals(tipoReferencia, that.tipoReferencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concepto, idReferencia, tipoReferencia, monto);
    }
}
