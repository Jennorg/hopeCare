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
        inicializarModulo("Clínica", "/sisgeho_schema.sql", "persona", DatabaseConnection::getClinicaConnection);
        migrarClinicaDatabase();
        
        // 2. Inicializar Autenticación
        inicializarModulo("Autenticación", "/sisgeho_schema.sql", "usuario", DatabaseConnection::getAuthConnection);
        migrarAuthDatabase();
        
        // 3. Inicializar Citas (Deshabilitado para este módulo)
        // inicializarModulo("Citas", "/citas_schema.sql", "cita", DatabaseConnection::getCitasConnection);
        // migrarCitasDatabase();

        // 4. Inicializar Facturación (Deshabilitado para este módulo)
        // inicializarModulo("Facturación", "/facturacion_schema.sql", "factura", DatabaseConnection::getFacturacionConnection);

        // 5. Inicializar Dashboard (Deshabilitado para este módulo)
        // inicializarModulo("Dashboard", "/dashboard_schema.sql", "paciente", DatabaseConnection::getDashboardConnection);
        // verificarYCargarDatosDashboard();

        // Cargar datos de prueba si están vacías
        verificarYCargarDatosPrueba();
    }

    private void migrarClinicaDatabase() {
        try (Connection conn = DatabaseConnection.getClinicaConnection();
             Statement stmt = conn.createStatement()) {
            
            boolean tieneFechaContratacion = false;
            try (ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM medico LIKE 'fecha_contratacion'")) {
                if (rs.next()) tieneFechaContratacion = true;
            }
            if (!tieneFechaContratacion) {
                System.out.println("Migrando tabla medico: agregando columna fecha_contratacion...");
                stmt.execute("ALTER TABLE medico ADD COLUMN fecha_contratacion DATE");
            }
        } catch (Exception e) {
            System.err.println("Error durante la migración de Clínica: " + e.getMessage());
        }
    }

    private void migrarAuthDatabase() {
        try (Connection conn = DatabaseConnection.getAuthConnection();
             Statement stmt = conn.createStatement()) {
            
            boolean tieneContrasena = false;
            boolean tieneRolStr = false;
            
            try (ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM usuario")) {
                while (rs.next()) {
                    String col = rs.getString("Field");
                    if ("contrasena".equalsIgnoreCase(col)) tieneContrasena = true;
                    if ("rol".equalsIgnoreCase(col)) tieneRolStr = true;
                }
            }
            
            if (!tieneContrasena) {
                System.out.println("Migrando tabla usuario: agregando columna contrasena...");
                stmt.execute("ALTER TABLE usuario ADD COLUMN contrasena VARCHAR(255) AFTER nombre_usuario");
            }
            if (!tieneRolStr) {
                System.out.println("Migrando tabla usuario: agregando columna rol...");
                stmt.execute("ALTER TABLE usuario ADD COLUMN rol VARCHAR(50) NOT NULL DEFAULT 'MEDICO'");
                stmt.execute("UPDATE usuario SET rol = 'ADMIN' WHERE nombre_usuario = 'admin'");
            }
        } catch (Exception e) {
            System.err.println("Error durante la migración de Autenticación: " + e.getMessage());
        }
    }

    private void migrarCitasDatabase() {
        try (Connection conn = DatabaseConnection.getCitasConnection();
             Statement stmt = conn.createStatement()) {
            
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
                System.out.println("Migrando tabla horario_atencion: agregando columna intervalo_minutos...");
                stmt.execute("ALTER TABLE horario_atencion ADD COLUMN intervalo_minutos INT DEFAULT 30");
            }

            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM horario_atencion")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Verificando integridad de horarios para médicos...");
                    try (Connection connClinica = DatabaseConnection.getClinicaConnection();
                         Statement stmtClinica = connClinica.createStatement();
                         ResultSet rsMed = stmtClinica.executeQuery("SELECT id_medico FROM medico WHERE activo = 1")) {
                        
                        String sqlCheck = "SELECT count(*) FROM horario_atencion WHERE id_medico = ? AND dia_semana = ?";
                        String sqlIns = "INSERT INTO horario_atencion (id_medico, dia_semana, hora_inicio, hora_fin, intervalo_minutos) VALUES (?, ?, ?, ?, ?)";
                        
                        try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
                             PreparedStatement psIns = conn.prepareStatement(sqlIns)) {
                            
                            while (rsMed.next()) {
                                int idMed = rsMed.getInt(1);
                                for (int dia = 1; dia <= 7; dia++) {
                                    psCheck.setInt(1, idMed);
                                    psCheck.setInt(2, dia);
                                    try (ResultSet rsC = psCheck.executeQuery()) {
                                        if (rsC.next() && rsC.getInt(1) == 0) {
                                            psIns.setInt(1, idMed);
                                            psIns.setInt(2, dia);
                                            psIns.setString(3, "08:00:00");
                                            psIns.setString(4, "12:00:00");
                                            psIns.setInt(5, 30);
                                            psIns.addBatch();
                                        }
                                    }
                                }
                            }
                            psIns.executeBatch();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error durante la migración de la base de datos de Citas: " + e.getMessage());
            e.printStackTrace();
        }
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
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
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
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
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
            stmt.execute("INSERT IGNORE INTO rol (id_rol, nombre_rol) VALUES (1, 'ADMIN')");
            stmt.execute("INSERT IGNORE INTO rol (id_rol, nombre_rol) VALUES (2, 'RECEPCIONISTA')");
            stmt.execute("INSERT IGNORE INTO rol (id_rol, nombre_rol) VALUES (3, 'MEDICO')");
            stmt.execute("INSERT IGNORE INTO especialidad (id_especialidad, nombre_especialidad) VALUES (1, 'Medicina General')");
            
            String hashedAdmin = com.esperanza.hopecare.util.Hasher.hash("admin123");
            stmt.execute("INSERT INTO usuario (nombre_usuario, contrasena, contrasena_hash, id_rol, id_persona, rol) " +
                         "VALUES ('admin', 'admin123', '" + hashedAdmin + "', 1, " + idAdmin + ", 'ADMIN')");
            
            stmt.execute("INSERT INTO paciente (id_persona, historia_clinica, alergias, grupo_sanguineo, contacto_emergencia) " +
                         "VALUES (" + idPac + ", 'HC001', 'Ninguna', 'O+', 'María Pérez - 987654321')");
            stmt.execute("INSERT INTO medico (id_persona, id_especialidad, registro_medico, activo) " +
                         "VALUES (" + idMed + ", 1, 'RM12345', 1)");
            stmt.execute("INSERT INTO cita (id_paciente, id_medico, fecha_hora, estado, motivo, creada_por) " +
                         "VALUES (1, 1, DATE_ADD(NOW(), INTERVAL 1 HOUR), 'PROGRAMADA', 'Control general', 1)");
        }
    }

    private boolean tablaExiste(Statement stmt, String nombre) throws Exception {
        try (ResultSet rs = stmt.executeQuery(
                "SHOW TABLES LIKE '" + nombre + "'")) {
            return rs.next();
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
