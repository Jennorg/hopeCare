package com.esperanza.hopecare.modules.citas_consultas.presenter;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.common.events.DatosFacturablesActualizadosEvent;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.NuevaConsultaEvent;

import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.model.Consulta;
import com.esperanza.hopecare.modules.citas_consultas.view.IConsultaView;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.ExamenLaboratorioDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.MedicamentoDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.SolicitudExamenDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.model.ExamenLaboratorio;
import com.esperanza.hopecare.modules.medicamentos_lab.model.SolicitudExamen;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ConsultaPresenter {
    private final IConsultaView view;
    private final ConsultaDAO consultaDAO;
    private final CitaDAO citaDAO;
    private final ExamenLaboratorioDAO examenLaboratorioDAO;
    private final SolicitudExamenDAO solicitudExamenDAO;
    private int idConsultaActual = -1;
    private int idPacienteActual = -1;
    private List<Cita> citasCargadas;

    public ConsultaPresenter(IConsultaView view) {
        this.view = view;
        this.consultaDAO = new ConsultaDAO();
        this.citaDAO = new CitaDAO();
        this.examenLaboratorioDAO = new ExamenLaboratorioDAO();
        this.solicitudExamenDAO = new SolicitudExamenDAO();
    }

    public void cargarCitasPendientes() {
        citasCargadas = citaDAO.obtenerCitasPorEstadoConNombres("PROGRAMADA");
        view.mostrarCitasPendientes(citasCargadas);
        view.actualizarEstadoAcciones(false);
    }

    public void seleccionarCita() {
        int idCita = view.getIdCitaSeleccionada();
        if (idCita <= 0) {
            view.mostrarError("Seleccione una cita.");
            return;
        }
        idConsultaActual = -1;
        idPacienteActual = -1;
        if (citasCargadas != null) {
            for (Cita c : citasCargadas) {
                if (c.getIdCita() == idCita) {
                    idPacienteActual = c.getIdPaciente();
                    break;
                }
            }
        }
        view.limpiarFormulario();
        view.actualizarEstadoAcciones(false);
        view.mostrarExito("Cita cargada, puede registrar la consulta.");
    }

    public void registrarConsulta() {
        int idCita = view.getIdCitaSeleccionada();
        String diagnostico = limpiarTexto(view.getDiagnostico());
        String sintomas = limpiarTexto(view.getSintomas());
        String tratamiento = limpiarTexto(view.getTratamiento());

        if (idCita <= 0) {
            view.mostrarError("Seleccione una cita primero.");
            return;
        }
        if (sintomas.isEmpty() || diagnostico.isEmpty()) {
            view.mostrarError("Síntomas y diagnóstico son obligatorios.");
            return;
        }
        double precio = view.getPrecio();
        if (precio < 0) {
            view.mostrarError("El precio no puede ser negativo.");
            return;
        }
        Consulta consulta = new Consulta(idCita, diagnostico, sintomas, tratamiento, false, precio);
        int idConsulta = consultaDAO.insertarConsultaYActualizarEstado(consulta);
        if (idConsulta > 0) {
            idConsultaActual = idConsulta;
            EventBus.getInstance().post(new NuevaConsultaEvent(idConsulta, idCita));
            EventBus.getInstance().post(new DatosFacturablesActualizadosEvent());
            view.actualizarEstadoAcciones(true);
            view.mostrarExito("Consulta registrada correctamente.");
            cargarCitasPendientes();
        } else {
            view.mostrarError("Error al registrar la consulta.");
        }
    }

    public void solicitarExamen() {
        if (idConsultaActual <= 0) {
            view.mostrarError("Primero guarde la consulta.");
            return;
        }
        if (idPacienteActual <= 0) {
            view.mostrarError("Error: no se pudo determinar el paciente.");
            return;
        }

        List<ExamenLaboratorio> examenes = examenLaboratorioDAO.listarTodos();
        if (examenes.isEmpty()) {
            view.mostrarError("No hay exámenes disponibles.");
            return;
        }

        Integer idExamen = view.solicitarExamen(examenes);
        if (idExamen == null || idExamen <= 0) {
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            SolicitudExamen solicitud = new SolicitudExamen(idPacienteActual, idExamen);
            if (solicitudExamenDAO.insertar(solicitud, conn)) {
                conn.commit();
                view.mostrarExito("Examen solicitado correctamente.");
            } else {
                conn.rollback();
                view.mostrarError("No se pudo solicitar el examen.");
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            view.mostrarError("Error de base de datos al solicitar examen.");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}