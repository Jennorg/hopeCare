package com.esperanza.hopecare;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.util.CargarDatosPrueba;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        inicializarBasesDatos();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/view/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/css/hopecare.css").toExternalForm());

        primaryStage.setTitle("HopeCare - Login");
        
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/esperanza/hopecare/imgs/logo.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la ventana: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void inicializarBasesDatos() {
        // 1. Inicializar Clínica (Base para todos)
        inicializarModulo("Clínica", "/clinica_schema.sql", "persona", DatabaseConnection::getClinicaConnection);
        
        // 2. Inicializar Autenticación
        inicializarModulo("Autenticación", "/auth_schema.sql", "usuario", DatabaseConnection::getAuthConnection);
        
        // 3. Inicializar Citas
        inicializarModulo("Citas", "/citas_schema.sql", "cita", DatabaseConnection::getCitasConnection);

        // Cargar datos de prueba si están vacías
        verificarYCargarDatosPrueba();
    }

    private void inicializarModulo(String nombre, String schemaPath, String tablaControl, ConnectionSupplier connSupplier) {
        try (Connection conn = connSupplier.get();
             Statement stmt = conn.createStatement()) {

            if (!tablaExiste(stmt, tablaControl)) {
                System.out.println("Base de datos [" + nombre + "] vacía o inexistente. Creando esquema...");
                ejecutarScriptSQL(conn, schemaPath);
                System.out.println("Esquema [" + nombre + "] creado exitosamente.");
            } else {
                System.out.println("Base de datos [" + nombre + "] ya inicializada.");
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar el módulo [" + nombre + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void verificarYCargarDatosPrueba() {
        try (Connection conn = DatabaseConnection.getAuthConnection();
             Statement stmt = conn.createStatement()) {
            if (baseDatosVacia(stmt)) {
                System.out.println("Insertando datos de prueba iniciales...");
                CargarDatosPrueba.main(new String[]{});
            }
        } catch (Exception e) {
            System.err.println("Error al verificar datos de prueba: " + e.getMessage());
        }
    }

    private boolean tablaExiste(Statement stmt, String nombre) throws Exception {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + nombre + "'")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean baseDatosVacia(Statement stmt) throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM usuario")) {
            return rs.next() && rs.getInt(1) == 0;
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

    @FunctionalInterface
    private interface ConnectionSupplier {
        Connection get() throws Exception;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
