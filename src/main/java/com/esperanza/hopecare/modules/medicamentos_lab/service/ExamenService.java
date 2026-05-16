package com.esperanza.hopecare.modules.medicamentos_lab.service;

import com.esperanza.hopecare.modules.medicamentos_lab.dao.ExamenLaboratorioDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.SolicitudExamenDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.model.ExamenLaboratorio;
import com.esperanza.hopecare.modules.medicamentos_lab.model.SolicitudExamen;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ExamenService {

    private ExamenLaboratorioDAO examenDAO;
    private SolicitudExamenDAO solicitudDAO;

    public ExamenService() {
        this.examenDAO = new ExamenLaboratorioDAO();
        this.solicitudDAO = new SolicitudExamenDAO();
    }

    public List<ExamenLaboratorio> listarExamenes() {
        return examenDAO.listarTodos();
    }

    public boolean solicitarExamen(int idPaciente, int idExamen) {
        SolicitudExamen solicitud = new SolicitudExamen(idPaciente, idExamen);
        try (Connection conn = DatabaseConnection.getConnection()) {
            return solicitudDAO.insertar(solicitud, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<SolicitudExamen> listarSolicitudesPendientes() {
        return solicitudDAO.listarPendientes();
    }

    public ExamenLaboratorio obtenerExamen(int idExamen) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return examenDAO.obtenerPorId(idExamen, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean agregarExamen(ExamenLaboratorio examen) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return examenDAO.insertar(examen, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<SolicitudExamen> listarSolicitudesPorEstado(String estado) {
        return solicitudDAO.listarPorEstado(estado);
    }

    public List<SolicitudExamen> listarTodasSolicitudes() {
        return solicitudDAO.listarTodas();
    }
}