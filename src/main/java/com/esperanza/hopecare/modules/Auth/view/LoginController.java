package com.esperanza.hopecare.modules.Auth.view;

import com.esperanza.hopecare.modules.Auth.dto.LoginDTO;
import com.esperanza.hopecare.modules.Auth.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    @FXML private Hyperlink     linkRegistro;

    @FXML private VBox          formContainer;
    @FXML private VBox          loadingContainer;
    @FXML private ProgressBar   progressCarga;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String usuario    = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        LoginDTO result = authService.login(usuario, contrasena);

        if (result.isExitoso()) {
            iniciarAnimacionCarga();
        } else {
            showError(result.getMensaje());
            txtContrasena.clear();
        }
    }

    private void iniciarAnimacionCarga() {
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
    }

    @FXML
    private void goToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/esperanza/hopecare/modules/Auth/view/signup.fxml")
            );
            BorderPane root = loader.load();

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            double x = stage.getX();
            double y = stage.getY();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();

            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(
                getClass().getResource("/com/esperanza/hopecare/main/hopecare.css")
                          .toExternalForm()
            );
            stage.setTitle("HopeCare – Crear Cuenta");
            stage.setScene(scene);
            stage.setResizable(true);

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
            showError("No se pudo abrir el formulario de registro.");
        }
    }

    private void abrirPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/esperanza/hopecare/main/main.fxml")
            );
            BorderPane root = loader.load();

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            double x = stage.getX();
            double y = stage.getY();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();

            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                getClass().getResource("/com/esperanza/hopecare/main/hopecare.css")
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
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: #c83232;");
    }
}
