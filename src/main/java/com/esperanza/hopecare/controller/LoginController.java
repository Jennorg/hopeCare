package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.util.SesionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    @FXML
    private void handleLogin() {
        String user = txtUsuario.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Por favor ingrese usuario y contraseña.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT u.id_usuario, u.nombre_usuario, r.nombre_rol " +
                     "FROM usuario u " +
                     "JOIN rol r ON u.id_rol = r.id_rol " +
                     "WHERE u.nombre_usuario = ? AND u.contrasena = ?")) {
            
            ps.setString(1, user);
            ps.setString(2, pass);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SesionManager.getInstance().iniciarSesion(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("nombre_rol")
                    );
                    cargarPantallaPrincipal();
                } else {
                    mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al conectar con la base de datos.");
        }
    }

    private void cargarPantallaPrincipal() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/view/main.fxml"));
        BorderPane root = loader.load();
        
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/css/hopecare.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("HopeCare - Módulo Pacientes y Médicos");
        stage.setMaximized(true);
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
