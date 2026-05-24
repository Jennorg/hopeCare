package com.esperanza.hopecare.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String AUTH_DB = "jdbc:sqlite:hopecare_auth.db";
    private static final String CLINICA_DB = "jdbc:sqlite:hopecare_clinica.db";
    private static final String CITAS_DB = "jdbc:sqlite:hopecare_citas.db";

    /**
     * Obtiene una conexión a la base de datos de Autenticación.
     */
    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(AUTH_DB);
    }

    /**
     * Obtiene una conexión a la base de datos de Clínica.
     */
    public static Connection getClinicaConnection() throws SQLException {
        return DriverManager.getConnection(CLINICA_DB);
    }

    /**
     * Obtiene una conexión a la base de datos de Citas.
     */
    public static Connection getCitasConnection() throws SQLException {
        return DriverManager.getConnection(CITAS_DB);
    }

    /**
     * Obtiene una conexión a la base de datos de Autenticación con la de Clínica ADJUNTA (ATTACH).
     * Útil para consultas que requieren unir datos de Usuario y Persona.
     */
    public static Connection getAuthWithClinicaConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(AUTH_DB);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ATTACH DATABASE 'hopecare_clinica.db' AS clinica");
        }
        return conn;
    }

    /**
     * Obtiene una conexión a la base de datos de Citas con las otras ADJUNTAS.
     * Útil para listados complejos de citas con nombres de médicos y pacientes.
     */
    public static Connection getCitasUnifiedConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(CITAS_DB);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ATTACH DATABASE 'hopecare_clinica.db' AS clinica");
            stmt.execute("ATTACH DATABASE 'hopecare_auth.db' AS auth");
        }
        return conn;
    }

    /**
     * Compatibilidad hacia atrás (apunta a auth por defecto).
     * @deprecated Usar métodos específicos por módulo.
     */
    @Deprecated
    public static Connection getConnection() throws SQLException {
        return getAuthConnection();
    }
}
