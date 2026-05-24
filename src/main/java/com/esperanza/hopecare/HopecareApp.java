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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        // 4. Inicializar Facturación
        inicializarModulo("Facturación", "/facturacion_schema.sql", "factura", DatabaseConnection::getFacturacionConnection);

        // 5. Inicializar Dashboard (schema completo unificado)
        inicializarModulo("Dashboard", "/dashboard_schema.sql", "paciente", DatabaseConnection::getDashboardConnection);
        verificarYCargarDatosDashboard();

        // Cargar datos de prueba si están vacías
        verificarYCargarDatosPrueba();
    }

    private void inicializarModulo(String nombre, String schemaPath, String tablaControl, ConnectionSupplier connSupplier) {
        try (Connection conn = connSupplier.get();
             Statement stmt = conn.createStatement()) {

            if (!tablaExiste(stmt, tablaControl)) {
                ejecutarScriptSQL(conn, schemaPath);
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
                CargarDatosPrueba.main(new String[]{});
            } else {
                try (Connection connClinica = DatabaseConnection.getClinicaConnection();
                     Statement stmtClinica = connClinica.createStatement()) {
                    ResultSet rs = stmtClinica.executeQuery("SELECT count(*) FROM persona");
                    if (rs.next() && rs.getInt(1) == 0) {
                        CargarDatosPrueba.main(new String[]{});
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al verificar datos de prueba: " + e.getMessage());
        }
    }

    private void verificarYCargarDatosDashboard() {
        try (Connection conn = DatabaseConnection.getDashboardConnection();
             Statement stmt = conn.createStatement()) {
            boolean necesitaPoblar = false;
            try {
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM paciente");
                necesitaPoblar = rs.next() && rs.getInt(1) < 1;
                if (!necesitaPoblar) {
                    rs = stmt.executeQuery("SELECT count(*) FROM medico");
                    necesitaPoblar = rs.next() && rs.getInt(1) < 1;
                }
                if (!necesitaPoblar) {
                    rs = stmt.executeQuery("SELECT count(*) FROM cita");
                    necesitaPoblar = rs.next() && rs.getInt(1) < 1;
                }
            } catch (SQLException e) {
                necesitaPoblar = true;
            }
            if (necesitaPoblar) {
                conn.setAutoCommit(false);
                try {
                    stmt.execute("PRAGMA foreign_keys = OFF");
                    stmt.execute("DELETE FROM detalle_factura");
                    stmt.execute("DELETE FROM factura");
                    stmt.execute("DELETE FROM consulta");
                    stmt.execute("DELETE FROM cita");
                    stmt.execute("DELETE FROM medicamento");
                    stmt.execute("DELETE FROM medico");
                    stmt.execute("DELETE FROM paciente");
                    stmt.execute("DELETE FROM usuario");
                    stmt.execute("DELETE FROM persona");
                    stmt.execute("DELETE FROM especialidad");
                    stmt.execute("DELETE FROM rol");
                    stmt.execute("DELETE FROM sqlite_sequence");
                    stmt.execute("PRAGMA foreign_keys = ON");
                    insertarDatosDashboard(conn);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al verificar datos de Dashboard: " + e.getMessage());
        }
    }

    private int insertarPersona(Connection conn, String nombre, String apellido, String documento, String telefono, String email) throws Exception {
        String sql = "INSERT INTO persona (nombre, apellido, documento_identidad, telefono, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, documento);
            ps.setString(4, telefono);
            ps.setString(5, email);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No se pudo obtener id de persona");
        }
    }

    private void insertarDatosDashboard(Connection conn) throws Exception {
        int idAdmin = insertarPersona(conn, "Admin", "Sistema", "00000001", "123456789", "admin@hopecare.com");
        int idPac = insertarPersona(conn, "Juan", "Pérez", "12345678", "987654321", "juan@email.com");
        int idMed = insertarPersona(conn, "Ana", "Martínez", "87654321", "678901234", "ana@email.com");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT OR IGNORE INTO rol (nombre_rol) VALUES ('ADMIN')");
            stmt.execute("INSERT OR IGNORE INTO rol (nombre_rol) VALUES ('RECEPCIONISTA')");
            stmt.execute("INSERT OR IGNORE INTO rol (nombre_rol) VALUES ('MEDICO')");
            stmt.execute("INSERT OR IGNORE INTO especialidad (nombre_especialidad) VALUES ('Medicina General')");
            stmt.execute("INSERT INTO usuario (nombre_usuario, contrasena_hash, id_rol, id_persona) " +
                         "VALUES ('admin', 'admin123', 1, " + idAdmin + ")");
            stmt.execute("INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia) " +
                         "VALUES (" + idPac + ", 'HC001', 'Ninguna', 'O+', 'María Pérez - 987654321')");
            stmt.execute("INSERT INTO medico (id_persona, id_especialidad, registro_medico, activo) " +
                         "VALUES (" + idMed + ", 1, 'RM12345', 1)");
            stmt.execute("INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, motivo, creada_por) " +
                         "VALUES (1, 1, datetime('now', '+1 hour'), 'PROGRAMADA', 'Control general', 1)");
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
