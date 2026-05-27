package com.esperanza.hopecare.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

public class InicializarBD {

    public static void ejecutar() {
        try (Connection conn = DatabaseConnection.getRootConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'hopecare_clinica'");
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = leerSQL("/hopecare_mysql_complete.sql");
                for (String s : sql.split(";")) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String leerSQL(String path) {
        InputStream is = InicializarBD.class.getResourceAsStream(path);
        if (is == null) throw new RuntimeException("No se encontró " + path);
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }
}
