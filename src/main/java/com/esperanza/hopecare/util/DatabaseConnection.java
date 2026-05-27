package com.esperanza.hopecare.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String PARAMS = "?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    private static Connection getConnection(String dbName) throws SQLException {
        return DriverManager.getConnection(BASE_URL + dbName + PARAMS, USER, PASSWORD);
    }

    public static Connection getAuthConnection() throws SQLException {
        return getConnection("hopecare_auth");
    }

    public static Connection getClinicaConnection() throws SQLException {
        return getConnection("hopecare_clinica");
    }

    public static Connection getCitasConnection() throws SQLException {
        return getConnection("hopecare_citas");
    }

    public static Connection getFacturacionConnection() throws SQLException {
        return getConnection("hopecare_facturacion");
    }

    /**
     * En MySQL, para hacer joins entre DBs, basta con usar una conexión a cualquiera
     * y referenciar las tablas como 'nombre_db.nombre_tabla'.
     * Por simplicidad, devolvemos una conexión general.
     */
    public static Connection getFacturacionUnifiedConnection() throws SQLException {
        return getFacturacionConnection();
    }

    public static Connection getAuthWithClinicaConnection() throws SQLException {
        return getAuthConnection();
    }

    public static Connection getCitasUnifiedConnection() throws SQLException {
        return getCitasConnection();
    }

    public static Connection getDashboardConnection() throws SQLException {
        return getConnection("hopecare_dashboard");
    }

    @Deprecated
    public static Connection getConnection() throws SQLException {
        return getAuthConnection();
    }
}
