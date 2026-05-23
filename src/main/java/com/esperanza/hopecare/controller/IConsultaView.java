package com.esperanza.hopecare.controller;
import com.esperanza.hopecare.model.Cita;
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
}
