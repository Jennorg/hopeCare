package com.esperanza.hopecare.modules.citas_consultas.presenter;

import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.HorarioAtencionDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.model.HorarioAtencion;
import com.esperanza.hopecare.modules.citas_consultas.view.ICitaView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.NuevaCitaEvent;

public class CitaPresenter {
    private ICitaView view;
    private HorarioAtencionDAO horarioDAO;
    private CitaDAO citaDAO;

    public CitaPresenter(ICitaView view) {
        this.view = view;
        this.horarioDAO = new HorarioAtencionDAO();
        this.citaDAO = new CitaDAO();
    }

    public void cargarCitasExistentes() {
        List<Cita> citas = citaDAO.listarTodasConNombres();
        view.mostrarCitasExistentes(citas);
    }

    public void cargarDiasDisponibles(int idMedico) {
        List<HorarioAtencion> horarios = horarioDAO.obtenerHorariosPorMedico(idMedico);
        List<Integer> diasSemana = new ArrayList<>();
        for (HorarioAtencion h : horarios) {
            if (!diasSemana.contains(h.getDiaSemana())) {
                diasSemana.add(h.getDiaSemana());
            }
        }
        Collections.sort(diasSemana);
        view.mostrarDiasDisponibles(diasSemana);
    }

    public void actualizarHorariosDisponibles(int idMedico, LocalDate fecha) {
        int diaSemana = fecha.getDayOfWeek().getValue();
        HorarioAtencion horario = horarioDAO.obtenerHorarioPorMedicoYDia(idMedico, diaSemana);

        if (horario == null) {
            view.mostrarMensajeError("El médico no atiende ese día.");
            view.mostrarHorariosDisponibles(new ArrayList<>());
            return;
        }

        List<LocalTime> todosBloques = generarBloques(
            horario.getHoraInicio(),
            horario.getHoraFin(),
            horario.getIntervaloMinutos()
        );

        List<Cita> citasOcupadas = citaDAO.obtenerCitasPorMedicoYFecha(idMedico, fecha);
        List<LocalTime> horariosOcupados = new ArrayList<>();
        for (Cita c : citasOcupadas) {
            horariosOcupados.add(c.getFechaHora().toLocalTime());
        }

        List<LocalTime> disponibles = new ArrayList<>();
        for (LocalTime bloque : todosBloques) {
            if (!horariosOcupados.contains(bloque)) {
                disponibles.add(bloque);
            }
        }

        view.mostrarHorariosDisponibles(disponibles);
    }

    private List<LocalTime> generarBloques(LocalTime inicio, LocalTime fin, int intervaloMinutos) {
        List<LocalTime> bloques = new ArrayList<>();
        LocalTime actual = inicio;
        while (!actual.isAfter(fin)) {
            bloques.add(actual);
            actual = actual.plusMinutes(intervaloMinutos);
        }
        return bloques;
    }

    public void reservarCita() {
        int idPaciente = view.getIdPacienteSeleccionado();
        int idMedico = view.getIdMedicoSeleccionado();
        LocalDate fecha = view.getFechaSeleccionada();
        LocalTime hora = view.getHoraSeleccionada();
        double precio = view.getPrecio();

        if (idPaciente <= 0 || idMedico <= 0 || fecha == null || hora == null) {
            view.mostrarMensajeError("Complete todos los campos.");
            return;
        }

        Cita nuevaCita = new Cita(idPaciente, idMedico, fecha.atTime(hora), "PROGRAMADA");
        boolean exito = citaDAO.insertarCita(nuevaCita);
        if (exito) {
            if (precio > 0) {
                new ConsultaDAO().insertarSiNoExiste(nuevaCita.getIdCita(), precio);
            }
            EventBus.getInstance().post(new NuevaCitaEvent(nuevaCita.getIdCita(), nuevaCita.getFechaHora()));
            view.mostrarMensajeExito("Cita reservada exitosamente.");
            view.limpiarCampos();
            actualizarHorariosDisponibles(idMedico, fecha);
        } else {
            view.mostrarMensajeError("Error al reservar la cita.");
        }
    }
}
