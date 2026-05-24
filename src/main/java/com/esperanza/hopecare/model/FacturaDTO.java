package com.esperanza.hopecare.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object inmutable para la factura.
 */
public final class FacturaDTO {
    private final int idPaciente;
    private final double subtotal;
    private final double impuesto;
    private final double total;
    private final String estadoPago;
    private final List<DetalleFacturaDTO> detalles;

    public FacturaDTO(int idPaciente, double subtotal, double impuesto, double total, 
                      String estadoPago, List<DetalleFacturaDTO> detalles) {
        this.idPaciente = idPaciente;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
        this.total = total;
        this.estadoPago = estadoPago;
        // Copia defensiva e inmutable
        this.detalles = List.copyOf(detalles);
    }

    // Getters (sin setters)
    public int getIdPaciente() { return idPaciente; }
    public double getSubtotal() { return subtotal; }
    public double getImpuesto() { return impuesto; }
    public double getTotal() { return total; }
    public String getEstadoPago() { return estadoPago; }
    public List<DetalleFacturaDTO> getDetalles() { return detalles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacturaDTO that = (FacturaDTO) o;
        return idPaciente == that.idPaciente &&
               Double.compare(that.subtotal, subtotal) == 0 &&
               Double.compare(that.impuesto, impuesto) == 0 &&
               Double.compare(that.total, total) == 0 &&
               Objects.equals(estadoPago, that.estadoPago) &&
               Objects.equals(detalles, that.detalles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPaciente, subtotal, impuesto, total, estadoPago, detalles);
    }

    @Override
    public String toString() {
        return "FacturaDTO{idPaciente=" + idPaciente + ", total=" + total + "}";
    }
}
