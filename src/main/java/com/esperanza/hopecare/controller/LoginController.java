package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.model.LoginDTO;
import com.esperanza.hopecare.service.AuthService;
import com.esperanza.hopecare.util.SesionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label         lblMensaje;
    @FXML private Button        btnIngresar;

    @FXML private VBox          formContainer;
    @FXML private VBox          loadingContainer;
    @FXML private ProgressIndicator progressCarga;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String usuario    = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            showError("Usuario y contraseña son requeridos.");
            return;
        }

        LoginDTO result = authService.login(usuario, contrasena);

        if (result.isExitoso()) {
            SesionManager.getInstance().iniciarSesion(usuario, result.getNombreRol(), result.getRol(), -1, result.getIdPersona());
            iniciarAnimacionCarga();
        } else {
            showError(result.getMensaje());
            txtContrasena.clear();
        }
    }

    private void iniciarAnimacionCarga() {
        if (formContainer != null && loadingContainer != null && progressCarga != null) {
            formContainer.setVisible(false);
            formContainer.setManaged(false);
            loadingContainer.setVisible(true);
            loadingContainer.setManaged(true);

            progressCarga.setProgress(0.0);

            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.ZERO,
                    new javafx.animation.KeyValue(progressCarga.progressProperty(), 0.0)
                ),
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(1.2),
                    new javafx.animation.KeyValue(progressCarga.progressProperty(), 1.0)
                )
            );
            timeline.setOnFinished(e -> javafx.application.Platform.runLater(this::abrirPrincipal));
            timeline.play();
        } else {
            abrirPrincipal();
        }
    }

    private void abrirPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/esperanza/hopecare/view/main.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            double x = stage.getX();
            double y = stage.getY();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();

            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                getClass().getResource("/com/esperanza/hopecare/css/hopecare.css")
                          .toExternalForm()
            );
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setTitle("HopeCare – Sistema de Gestión Hospitalaria");

            if (max) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            } else {
                stage.setMaximized(false);
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(w);
                stage.setHeight(h);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al cargar la pantalla principal.");
        }
    }

    private void showError(String mensaje) {
        if (lblMensaje != null) {
            if (mensaje == null || mensaje.trim().isEmpty()) {
                lblMensaje.setText("");
                lblMensaje.getStyleClass().remove("msg-error-active");
                lblMensaje.setVisible(false);
                lblMensaje.setManaged(false);
            } else {
                lblMensaje.setText(mensaje);
                lblMensaje.setStyle("");
                if (!lblMensaje.getStyleClass().contains("msg-error-active")) {
                    lblMensaje.getStyleClass().add("msg-error-active");
                }
                lblMensaje.setVisible(true);
                lblMensaje.setManaged(true);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
    }
}
