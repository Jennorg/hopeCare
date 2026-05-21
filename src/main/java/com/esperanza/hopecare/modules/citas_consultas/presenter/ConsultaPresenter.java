package com.esperanza.hopecare.modules.citas_consultas.presenter;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.NuevaConsultaEvent;

import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.model.Consulta;
import com.esperanza.hopecare.modules.citas_consultas.view.IConsultaView;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ConsultaPresenter {
    private final IConsultaView view;
    private final ConsultaDAO consultaDAO;
    private final CitaDAO citaDAO;
    private int idConsultaActual = -1;
    private int idPacienteActual = -1;
    private List<Cita> citasCargadas;

    public ConsultaPresenter(IConsultaView view) {
        this.view = view;
        this.consultaDAO = new ConsultaDAO();
        this.citaDAO = new CitaDAO();
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
        Consulta consulta = new Consulta(idCita, diagnostico, sintomas, tratamiento, precio);
        int idConsulta = consultaDAO.insertarConsultaYActualizarEstado(consulta);
        if (idConsulta > 0) {
            idConsultaActual = idConsulta;
            EventBus.getInstance().post(new NuevaConsultaEvent(idConsulta, idCita));
            view.actualizarEstadoAcciones(true);
            view.mostrarExito("Consulta registrada correctamente.");
            cargarCitasPendientes();
        } else {
            view.mostrarError("Error al registrar la consulta.");
        }
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
