package com.esperanza.hopecare.model;

public class FacturaResumenDTO {
    private final int idFactura;
    private final String pacienteNombre;
    private final String fechaEmision;
    private final double subtotal;
    private final double impuesto;
    private final double total;
    private final String estadoPago;

    public FacturaResumenDTO(int idFactura, String pacienteNombre, String fechaEmision, double subtotal, double impuesto, double total, String estadoPago) {
        this.idFactura = idFactura;
        this.pacienteNombre = pacienteNombre;
        this.fechaEmision = fechaEmision;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
        this.total = total;
        this.estadoPago = estadoPago;
    }

    public int getIdFactura() { return idFactura; }
    public String getPacienteNombre() { return pacienteNombre; }
    public String getFechaEmision() { return fechaEmision; }
    public double getSubtotal() { return subtotal; }
    public double getImpuesto() { return impuesto; }
    public double getTotal() { return total; }
    public String getEstadoPago() { return estadoPago; }
}
