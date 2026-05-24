package com.esperanza.hopecare.modules.facturacion.service;

import com.esperanza.hopecare.modules.facturacion.dto.DetalleFacturaDTO;
import com.esperanza.hopecare.modules.facturacion.dto.FacturaDTO;
import com.esperanza.hopecare.modules.facturacion.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.facturacion.dao.FacturaDAO;
import com.esperanza.hopecare.modules.facturacion.dao.DetalleFacturaDAO;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.DatosFacturablesActualizadosEvent;

public class FacturacionService {

    private static final double TASA_IMPUESTO = 0.19;

    private ConsultaDAO consultaDAO;
    private FacturaDAO facturaDAO;
    private DetalleFacturaDAO detalleFacturaDAO;

    public FacturacionService() {
        this.consultaDAO = new ConsultaDAO();
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
            List<DetalleFacturaDTO> detallesPendientes = obtenerDetallesPendientes(idPaciente, conn);

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

    public FacturaDTO generarFactura(int idPaciente) {
        return generarFactura(idPaciente, null);
    }

    public FacturaDTO generarFactura(int idPaciente, String tipo) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            List<DetalleFacturaDTO> detallesPendientes = obtenerDetallesPendientes(idPaciente, conn);

            if (detallesPendientes.isEmpty()) {
                conn.rollback();
                return null;
            }

            double subtotal = detallesPendientes.stream().mapToDouble(DetalleFacturaDTO::getMonto).sum();
            double impuesto = subtotal * TASA_IMPUESTO;
            double total = subtotal + impuesto;

            String estadoPago = "PENDIENTE";
            int idFactura = facturaDAO.insertarFactura(idPaciente, subtotal, impuesto, total, estadoPago, conn);
            if (idFactura == -1) throw new SQLException("No se pudo insertar la factura");

            for (DetalleFacturaDTO detalle : detallesPendientes) {
                boolean okDetalle = detalleFacturaDAO.insertarDetalle(idFactura, detalle, conn);
                if (!okDetalle) throw new SQLException("Error al insertar detalle: " + detalle.getConcepto());

                boolean okActualizar = marcarConceptoFacturado(detalle, conn);
                if (!okActualizar) throw new SQLException("Error al actualizar facturado para: " + detalle.getConcepto());
            }

            conn.commit();
            EventBus.getInstance().post(new DatosFacturablesActualizadosEvent());

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

    private boolean marcarConceptoFacturado(DetalleFacturaDTO detalle, Connection conn) throws SQLException {
        return consultaDAO.marcarFacturado(detalle.getIdReferencia(), conn);
    }

    private List<DetalleFacturaDTO> obtenerDetallesPendientes(int idPaciente, Connection conn) throws SQLException {
        List<DetalleFacturaDTO> detalles = new ArrayList<>();

        List<Object[]> consultas = consultaDAO.listarNoFacturadasPorPaciente(idPaciente, conn);
        for (Object[] c : consultas) {
            int idConsulta = (int) c[0];
            double monto = (double) c[1];
            detalles.add(new DetalleFacturaDTO("Consulta médica #" + idConsulta, idConsulta, "CONSULTA", monto));
        }

        return detalles;
    }
}
