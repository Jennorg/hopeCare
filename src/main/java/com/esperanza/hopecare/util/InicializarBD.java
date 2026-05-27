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
                         "id_horario INT AUTO_INCREMENT PRIMARY KEY, " +
                         "id_medico INT NOT NULL, " +
                         "dia_semana INT NOT NULL, " +
                         "hora_inicio TIME NOT NULL, " +
                         "hora_fin TIME NOT NULL, " +
                         "intervalo_minutos INT DEFAULT 30, " +
                         "activo TINYINT(1) DEFAULT 1) ENGINE=InnoDB");
            stmt.execute("CREATE TABLE IF NOT EXISTS cita (" +
                         "id_cita INT AUTO_INCREMENT PRIMARY KEY, " +
                         "id_paciente INT NOT NULL, " +
                         "id_medico INT NOT NULL, " +
                         "fecha_hora DATETIME NOT NULL, " +
                         "estado VARCHAR(20) NOT NULL, " +
                         "motivo TEXT, " +
                         "creada_por INT NOT NULL, " +
                         "fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");
            stmt.execute("CREATE TABLE IF NOT EXISTS consulta (" +
                         "id_consulta INT AUTO_INCREMENT PRIMARY KEY, " +
                         "id_cita INT NOT NULL UNIQUE, " +
                         "diagnostico TEXT, sintomas TEXT, tratamiento TEXT, notas_medicas TEXT, " +
                         "fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                         "precio DECIMAL(10,2) NOT NULL DEFAULT 0.0, facturado TINYINT(1) DEFAULT 0) ENGINE=InnoDB");
            
            boolean tieneIntervalo = false;
            try (ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM horario_atencion LIKE 'intervalo_minutos'")) {
                if (rs.next()) {
                    tieneIntervalo = true;
                }
            }
            if (!tieneIntervalo) {
                stmt.execute("ALTER TABLE horario_atencion ADD COLUMN intervalo_minutos INT DEFAULT 30");
            }
        }
    }

    @FunctionalInterface
    private interface ConnectionSupplier {
        Connection get() throws SQLException;
    }
}
