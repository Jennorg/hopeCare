package com.esperanza.hopecare.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String PARAMS = "?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return getClinicaConnection();
    }

    public static Connection getRootConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/?serverTimezone=UTC", USER, PASSWORD);
    }

    public static Connection getClinicaConnection() throws SQLException {
        return DriverManager.getConnection(BASE_URL + "hopecare_clinica" + PARAMS, USER, PASSWORD);
    }

    public static Connection getCitasUnifiedConnection() throws SQLException {
        return getClinicaConnection();
    }
}
