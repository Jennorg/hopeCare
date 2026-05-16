package com.esperanza.hopecare.modules.medicamentos_lab.facade;

import com.esperanza.hopecare.modules.medicamentos_lab.dao.*;
import com.esperanza.hopecare.modules.medicamentos_lab.model.*;
import com.esperanza.hopecare.common.events.DatosFacturablesActualizadosEvent;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.utils.RoleValidator;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class GestionClinicaFacade {

    private MedicamentoDAO medicamentoDAO;
    private EntregaMedicamentoDAO entregaDAO;
    private SolicitudExamenDAO solicitudDAO;
    private RoleValidator roleValidator;

    public GestionClinicaFacade() {
        this.medicamentoDAO = new MedicamentoDAO();
        this.entregaDAO = new EntregaMedicamentoDAO();
        this.solicitudDAO = new SolicitudExamenDAO();
        this.roleValidator = new RoleValidator();
    }

    public boolean procesarEntregaMedicamento(int idPaciente, int idMedicamento, int cantidad, boolean presenteReceta, String rolUsuario) {
        if (!roleValidator.tieneRol(rolUsuario, "FARMACIA") && !roleValidator.tieneRol(rolUsuario, "ADMIN")) {
            System.err.println("Acceso denegado: rol " + rolUsuario + " no puede procesar entregas.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Medicamento med = medicamentoDAO.obtenerPorId(idMedicamento, conn);
            if (med == null || med.getStockActual() < cantidad) {
                throw new RuntimeException("Stock insuficiente o medicamento no encontrado.");
            }

            EntregaMedicamento entrega = new EntregaMedicamento(idPaciente, idMedicamento, cantidad, presenteReceta, 1);
            boolean okEntrega = entregaDAO.insertar(entrega, conn);
            if (!okEntrega) throw new RuntimeException("Error al registrar entrega.");

            int nuevoStock = med.getStockActual() - cantidad;
            boolean okStock = medicamentoDAO.actualizarStock(idMedicamento, nuevoStock, conn);
            if (!okStock) throw new RuntimeException("Error al actualizar stock.");

            conn.commit();
            EventBus.getInstance().post(new DatosFacturablesActualizadosEvent());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean registrarResultadoExamen(int idSolicitud, String resultado, String estado, String rolUsuario) {
        if (!roleValidator.tieneRol(rolUsuario, "LABORATORIO") && !roleValidator.tieneRol(rolUsuario, "ADMIN")) {
            System.err.println("Acceso denegado: rol " + rolUsuario + " no puede registrar resultados.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean ok = solicitudDAO.actualizarResultado(idSolicitud, resultado, estado, conn);
            if (ok && "COMPLETADO".equals(estado)) {
                EventBus.getInstance().post(new DatosFacturablesActualizadosEvent());
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}