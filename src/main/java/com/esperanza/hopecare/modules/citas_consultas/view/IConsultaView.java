package com.esperanza.hopecare.modules.citas_consultas.view;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.medicamentos_lab.model.ExamenLaboratorio;
import java.util.List;

public interface IConsultaView {
    int getIdCitaSeleccionada();
    String getDiagnostico();
    String getSintomas();
    String getTratamiento();
    double getPrecio();
    void mostrarCitasPendientes(List<Cita> citas);
    void mostrarError(String mensaje);
    void mostrarExito(String mensaje);
    void limpiarFormulario();
    void limpiarSeleccionCita();
    void actualizarEstadoAcciones(boolean consultaGuardada);
    Integer solicitarExamen(List<ExamenLaboratorio> examenesDisponibles);
}