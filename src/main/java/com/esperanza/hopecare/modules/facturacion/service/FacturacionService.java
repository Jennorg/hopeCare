package com.esperanza.hopecare.modules.facturacion.service;

import com.esperanza.hopecare.modules.facturacion.dto.DetalleFacturaDTO;
import com.esperanza.hopecare.modules.facturacion.dto.FacturaDTO;
import com.esperanza.hopecare.modules.facturacion.dao.*;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.NuevaFacturaEvent;

/**
 * Servicio de facturación que orquesta la creación de facturas a partir de
 * conceptos pendientes (consultas no facturadas, exámenes, medicamentos entregados).
 */
public class FacturacionService {

    private static final double TASA_IMPUESTO = 0.19; // 19% IVA

    private ConsultaDAO consultaDAO;
    private SolicitudExamenDAO solicitudExamenDAO;
    private EntregaMedicamentoDAO entregaDAO;
    private FacturaDAO facturaDAO;
    private DetalleFacturaDAO detalleFacturaDAO;

    public FacturacionService() {
        this.consultaDAO = new ConsultaDAO();
        this.solicitudExamenDAO = new SolicitudExamenDAO();
        this.entregaDAO = new EntregaMedicamentoDAO();
        this.facturaDAO = new FacturaDAO();
        this.detalleFacturaDAO = new DetalleFacturaDAO();
    }

    public FacturaDTO previsualizarFactura(int idPaciente) {
        return previsualizarFactura(idPaciente, null);
    }

    public FacturaDTO previsualizarFactura(int idPaciente, String tipo) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            List<DetalleFacturaDTO> detallesPendientes = obtenerDetallesPendientes(idPaciente, tipo, conn);

            if (detallesPendientes.isEmpty()) {
                return null;
            }

            double subtotal = detallesPendientes.stream().mapToDouble(DetalleFacturaDTO::getMonto).sum();
            double impuesto = subtotal * TASA_IMPUESTO;
            double total = subtotal + impuesto;

            return new FacturaDTO(idPaciente, subtotal, impuesto, total, "PENDIENTE", detallesPendientes);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Genera una factura para un paciente a partir de todos los conceptos pendientes:
     * - Consultas médicas con facturado = 0
     * - Solicitudes de examen con facturado = 0 (se asume campo en solicitud_examen)
     * - Entregas de medicamentos con facturado = 0 (se asume campo en entrega_medicamento)
     *
     * @param idPaciente Identificador del paciente
     * @return FacturaDTO con los detalles generados, o null si no hay pendientes o error
     */
    public FacturaDTO generarFactura(int idPaciente) {
        return generarFactura(idPaciente, null);
    }

    public FacturaDTO generarFactura(int idPaciente, String tipo) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            List<DetalleFacturaDTO> detallesPendientes = obtenerDetallesPendientes(idPaciente, tipo, conn);

            if (detallesPendientes.isEmpty()) {
                conn.rollback();
                return null;
            }

            double subtotal = detallesPendientes.stream().mapToDouble(DetalleFacturaDTO::getMonto).sum();
            double impuesto = subtotal * TASA_IMPUESTO;
            double total = subtotal + impuesto;

            // Insertar cabecera de factura
            String estadoPago = "PENDIENTE";
            int idFactura = facturaDAO.insertarFactura(idPaciente, subtotal, impuesto, total, estadoPago, conn);
            if (idFactura == -1) throw new SQLException("No se pudo insertar la factura");

            // Insertar cada detalle y actualizar el estado facturado de los conceptos origen
            for (DetalleFacturaDTO detalle : detallesPendientes) {
                boolean okDetalle = detalleFacturaDAO.insertarDetalle(idFactura, detalle, conn);
                if (!okDetalle) throw new SQLException("Error al insertar detalle: " + detalle.getConcepto());

                // Marcar el concepto original como facturado
                boolean okActualizar = marcarConceptoFacturado(detalle, conn);
                if (!okActualizar) throw new SQLException("Error al actualizar facturado para: " + detalle.getConcepto());
            }

            conn.commit();
            EventBus.getInstance().post(new NuevaFacturaEvent(idFactura, total));

            // Retornar el DTO inmutable
            return new FacturaDTO(idPaciente, subtotal, impuesto, total, estadoPago, detallesPendientes);

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Marcar el concepto original (consulta, examen o medicamento) como facturado (=1).
     */
    private boolean marcarConceptoFacturado(DetalleFacturaDTO detalle, Connection conn) throws SQLException {
        String tipo = detalle.getTipoReferencia();
        int idReferencia = detalle.getIdReferencia();
        switch (tipo) {
            case "CONSULTA":
                return consultaDAO.marcarFacturado(idReferencia, conn);
            case "EXAMEN":
                return solicitudExamenDAO.marcarFacturado(idReferencia, conn);
            case "MEDICAMENTO":
                return entregaDAO.marcarFacturado(idReferencia, conn);
            default:
                return false;
        }
    }

    private List<DetalleFacturaDTO> obtenerDetallesPendientes(int idPaciente, String tipo, Connection conn) throws SQLException {
        List<DetalleFacturaDTO> detalles = new ArrayList<>();

        if (tipo == null || tipo.equals("CONSULTA")) {
            List<Object[]> consultas = consultaDAO.listarNoFacturadasPorPaciente(idPaciente, conn);
            for (Object[] c : consultas) {
                int idConsulta = (int) c[0];
                double monto = (double) c[1];
                detalles.add(new DetalleFacturaDTO("Consulta médica #" + idConsulta, idConsulta, "CONSULTA", monto));
            }
        }

        if (tipo == null || tipo.equals("EXAMEN")) {
            List<Object[]> examenes = solicitudExamenDAO.listarNoFacturadasPorPaciente(idPaciente, conn);
            for (Object[] e : examenes) {
                int idExamen = (int) e[0];
                double monto = (double) e[1];
                detalles.add(new DetalleFacturaDTO("Examen de laboratorio #" + idExamen, idExamen, "EXAMEN", monto));
            }
        }

        if (tipo == null || tipo.equals("MEDICAMENTO")) {
            List<Object[]> medicamentos = entregaDAO.listarNoFacturadosPorPaciente(idPaciente, conn);
            for (Object[] m : medicamentos) {
                int idEntrega = (int) m[0];
                double monto = (double) m[1];
                detalles.add(new DetalleFacturaDTO("Medicamento entregado #" + idEntrega, idEntrega, "MEDICAMENTO", monto));
            }
        }

        return detalles;
    }
}
