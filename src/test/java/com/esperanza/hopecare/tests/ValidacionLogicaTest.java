package com.esperanza.hopecare.tests;

import com.esperanza.hopecare.util.ValidadorDatos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidacionLogicaTest {

    @Test
    public void testValidacionEmail() {
        assertTrue(ValidadorDatos.esEmailValido("test@hopecare.com"), "Email válido debería ser aceptado");
        assertFalse(ValidadorDatos.esEmailValido("test@com"), "Email sin dominio completo debería ser rechazado");
        assertFalse(ValidadorDatos.esEmailValido("test.com"), "Email sin @ debería ser rechazado");
        assertFalse(ValidadorDatos.esEmailValido(""), "Email vacío debería ser rechazado");
    }

    @Test
    public void testValidacionSoloNumeros() {
        assertTrue(ValidadorDatos.esSoloNumeros("12345678"), "Cadena numérica debería ser aceptada");
        assertFalse(ValidadorDatos.esSoloNumeros("1234A678"), "Cadena con letras debería ser rechazada");
        assertFalse(ValidadorDatos.esSoloNumeros("123-456"), "Cadena con guiones debería ser rechazada");
    }

    @Test
    public void testControlLogicoAgendas() {
        assertTrue(ValidadorDatos.esHorarioCoherente("08:00", "12:00"), "Horario normal debería ser aceptado");
        assertFalse(ValidadorDatos.esHorarioCoherente("14:00", "10:00"), "Hora de cierre previa al inicio debería ser rechazada");
        assertFalse(ValidadorDatos.esHorarioCoherente("09:00", "09:00"), "Horas iguales deberían ser rechazadas");
    }
}
