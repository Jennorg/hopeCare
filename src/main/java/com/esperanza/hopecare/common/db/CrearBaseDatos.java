package com.esperanza.hopecare.common.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class CrearBaseDatos {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getRootConnection();
             Statement stmt = conn.createStatement()) {

            InputStream is = CrearBaseDatos.class.getResourceAsStream("/hopecare_mysql_complete.sql");
            if (is == null) {
                System.err.println("No se encontró el archivo hopecare_mysql_complete.sql en resources");
                return;
            }
            String sqlScript = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            for (String sentencia : sqlScript.split(";")) {
                String sql = sentencia.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                    System.out.println("Ejecutado: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                }
            }

            System.out.println("\n*** Base de datos MySQL creada/actualizada correctamente ***");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
