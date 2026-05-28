package com.esperanza.hopecare.util;

import java.util.regex.Pattern;

public class ValidadorDatos {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean esSoloNumeros(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        return str.matches("^[0-9]+$");
    }

    public static boolean esHorarioCoherente(String horaInicio, String horaFin) {
        if (horaInicio == null || horaFin == null) return false;
        try {
            String[] ini = horaInicio.split(":");
            String[] fin = horaFin.split(":");
            int minIni = Integer.parseInt(ini[0]) * 60 + Integer.parseInt(ini[1]);
            int minFin = Integer.parseInt(fin[0]) * 60 + Integer.parseInt(fin[1]);
            return minFin > minIni;
        } catch (Exception e) {
            return false;
        }
    }
}
