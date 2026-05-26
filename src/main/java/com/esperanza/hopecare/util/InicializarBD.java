package com.esperanza.hopecare.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class InicializarBD {

    public static void ejecutar() {
        try {
            ejecutarSchema("/clinica_schema.sql", DatabaseConnection::getClinicaConnection);
            ejecutarSchema("/auth_schema.sql", DatabaseConnection::getAuthConnection);
            ejecutarSchema("/citas_schema.sql", DatabaseConnection::getCitasConnection);
            migrarCitas();
            ejecutarSchema("/facturacion_schema.sql", DatabaseConnection::getFacturacionConnection);
            ejecutarSchema("/dashboard_schema.sql", DatabaseConnection::getDashboardConnection);
        } catch (Exception e) {
            System.err.println("Error al inicializar BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ejecutarSchema(String path, ConnectionSupplier supplier) throws SQLException {
        try (Connection conn = supplier.get(); Statement stmt = conn.createStatement()) {
            String sql = leerSQL(path);
            for (String s : sql.split(";")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private static String leerSQL(String path) {
        InputStream is = InicializarBD.class.getResourceAsStream(path);
        if (is == null) throw new RuntimeException("No se encontró " + path);
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }

    private static void migrarCitas() throws SQLException {
        try (Connection conn = DatabaseConnection.getCitasConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS horario_atencion (" +
                         "id_horario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "id_medico INTEGER NOT NULL, " +
                         "dia_semana INTEGER NOT NULL, " +
                         "hora_inicio TEXT NOT NULL, " +
                         "hora_fin TEXT NOT NULL, " +
                         "activo INTEGER DEFAULT 1)");
            stmt.execute("CREATE TABLE IF NOT EXISTS cita (" +
                         "id_cita INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "id_paciente INTEGER NOT NULL, " +
                         "id_medico INTEGER NOT NULL, " +
                         "fecha_hora DATETIME NOT NULL, " +
                         "estado TEXT NOT NULL, " +
                         "motivo TEXT, " +
                         "creada_por INTEGER NOT NULL, " +
                         "fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS consulta (" +
                         "id_consulta INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "id_cita INTEGER NOT NULL UNIQUE, " +
                         "diagnostico TEXT, sintomas TEXT, tratamiento TEXT, notas_medicas TEXT, " +
                         "fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                         "precio REAL NOT NULL DEFAULT 0.0, facturado INTEGER DEFAULT 0)");
            boolean tieneIntervalo = false;
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(horario_atencion)")) {
                while (rs.next()) {
                    if ("intervalo_minutos".equalsIgnoreCase(rs.getString("name"))) {
                        tieneIntervalo = true;
                        break;
                    }
                }
            }
            if (!tieneIntervalo) {
                stmt.execute("ALTER TABLE horario_atencion ADD COLUMN intervalo_minutos INTEGER DEFAULT 30");
            }
        }
    }

    @FunctionalInterface
    private interface ConnectionSupplier {
        Connection get() throws SQLException;
    }
}
