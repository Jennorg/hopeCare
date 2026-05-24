package com.esperanza.hopecare.util;

import java.time.LocalDateTime;

public class NuevaCitaEvent {
    private final int idCita;
    private final LocalDateTime fechaHora;

    public NuevaCitaEvent(int idCita, LocalDateTime fechaHora) {
        this.idCita = idCita;
        this.fechaHora = fechaHora;
    }

    public int getIdCita() { return idCita; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
