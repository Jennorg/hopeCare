package com.esperanza.hopecare.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Ajustar segun configuracion local

    public static Connection getConnection(String dbName) throws SQLException {
        return DriverManager.getConnection(BASE_URL + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", USER, PASSWORD);
    }

    public static Connection getClinicaConnection() throws SQLException {
        return getConnection("hopecare_clinica");
    }

    public static Connection getAuthConnection() throws SQLException {
        return getClinicaConnection();
    }

    public static Connection getCitasConnection() throws SQLException {
        return getClinicaConnection();
    }

    public static Connection getFacturacionConnection() throws SQLException {
        return getClinicaConnection();
    }

    public static Connection getDashboardConnection() throws SQLException {
        return getClinicaConnection();
    }

    @Deprecated
    public static Connection getConnection() throws SQLException {
        return getClinicaConnection();
    }
}
