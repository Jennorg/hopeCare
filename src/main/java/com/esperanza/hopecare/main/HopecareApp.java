package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void inicializarBaseDatos() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (!tablaExiste(stmt, "persona")) {
                System.out.println("Base de datos vacía. Creando esquema...");
                ejecutarScriptSQL(conn, "/sisgeho_schema.sql");
                System.out.println("Esquema creado. Insertando datos de prueba...");
                com.esperanza.hopecare.common.db.CargarDatosPrueba.main(new String[]{});
            } else {
                System.out.println("Base de datos existente. Verificando schema...");
                try {
                    if (!schemaEstaSincronizado(stmt)) {
                        System.err.println("ERROR: El schema de la BD no es compatible con esta versión del código.");
                        System.err.println("Solución: borra el archivo 'sisgeho.db' y reinicia la aplicación.");
                    }
                } catch (Exception e) {
                    System.err.println("Error al verificar el schema: " + e.getMessage());
                    e.printStackTrace();
                }

                if (!columnaExiste(stmt, "consulta", "precio")) {
                    System.out.println("Migrando: agregando columna precio a consulta...");
                    stmt.execute("ALTER TABLE consulta ADD COLUMN precio REAL NOT NULL DEFAULT 0.0");
                }

                if (!columnaExiste(stmt, "medico", "precio_consulta")) {
                    System.out.println("Migrando: agregando columna precio_consulta a medico...");
                    stmt.execute("ALTER TABLE medico ADD COLUMN precio_consulta REAL NOT NULL DEFAULT 0.0");
                }
                
                if (baseDatosVacia(stmt)) {
                    System.out.println("Insertando datos de prueba...");
                    com.esperanza.hopecare.common.db.CargarDatosPrueba.main(new String[]{});
                }
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean schemaEstaSincronizado(Statement stmt) throws Exception {
        if (!columnaExiste(stmt, "usuario", "id_persona")) {
            System.out.println("[DB] Falta columna 'id_persona' en tabla 'usuario'.");
            return false;
        }
        if (columnaExiste(stmt, "persona", "id_usuario")) {
            System.out.println("[DB] Columna obsoleta 'id_usuario' en tabla 'persona'.");
            return false;
        }
        System.out.println("[DB] Schema sincronizado.");
        return true;
    }

    private boolean tablaExiste(Statement stmt, String nombre) throws Exception {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + nombre + "'")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean columnaExiste(Statement stmt, String tabla, String columna) throws Exception {
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tabla + ")")) {
            while (rs.next()) {
                if (columna.equals(rs.getString("name"))) return true;
            }
        }
        return false;
    }

    private boolean baseDatosVacia(Statement stmt) throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM persona")) {
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

    public static void main(String[] args) {
        launch(args);
    }
}
