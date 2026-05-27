package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

public class HopecareApp extends Application {

    @Override
    public void init() {
        inicializarBaseDatos();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/modules/Auth/view/login.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        scene.setFill(Color.web("#f5f7fa"));
        scene.getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        primaryStage.setTitle("HopeCare - Sistema de Gestión Hospitalaria");

        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/esperanza/hopecare/imgs/logo.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void inicializarBaseDatos() {
        try (Connection conn = DatabaseConnection.getRootConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'hopecare_clinica'");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Inicializando todas las bases de datos desde hopecare_mysql_complete.sql...");
                ejecutarScriptSQL(conn, "/hopecare_mysql_complete.sql");
                System.out.println("Bases de datos inicializadas correctamente.");
                System.out.println("Insertando datos de prueba...");
                com.esperanza.hopecare.common.db.CargarDatosPrueba.main(new String[]{});
            } else {
                System.out.println("Las bases de datos ya existen, se omite la creación.");
                verificarYCargarDatosPrueba();
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void verificarYCargarDatosPrueba() {
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM persona");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Insertando datos de prueba...");
                com.esperanza.hopecare.common.db.CargarDatosPrueba.main(new String[]{});
            }
        } catch (Exception e) {
            System.err.println("Error al verificar datos de prueba: " + e.getMessage());
        }
    }

    private void ejecutarScriptSQL(Connection conn, String resourcePath) throws Exception {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("No se encontró el archivo " + resourcePath);
        }
        String sqlScript = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
        try (Statement stmt = conn.createStatement()) {
            for (String sentencia : sqlScript.split(";")) {
                String sql = sentencia.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
