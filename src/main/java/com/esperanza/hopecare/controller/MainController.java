package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.util.SesionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabPacientes;
    @FXML private Tab tabMedicos;
    @FXML private Label lblBreadcrumb;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    @FXML private Hyperlink linkPacientes;
    @FXML private Hyperlink linkMedicos;

    @FXML private HBox headerTop;
    @FXML private FlowPane navLinks;
    @FXML private FlowPane headerMiddle;
    @FXML private Pane spacer1;
    @FXML private Pane spacer2;

    @FXML
    public void initialize() {
        SesionManager sesion = SesionManager.getInstance();
        lblUserName.setText(sesion.getNombreUsuario());
        lblUserRole.setText(sesion.getNombreRol());

        mainTabPane.getSelectionModel().select(tabPacientes);
        actualizarEnlacesActivos(linkPacientes);
        lblBreadcrumb.setText("Inicio > Pacientes");

        // Add listener for responsive window resizing
        mainTabPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0) {
                ajustarNavbar(newVal.doubleValue());
            }
        });
    }

    @FXML
    private void navigateToPacientes() {
        mainTabPane.getSelectionModel().select(tabPacientes);
        lblBreadcrumb.setText("Inicio > Pacientes");
        actualizarEnlacesActivos(linkPacientes);
    }

    @FXML
    private void navigateToMedicos() {
        mainTabPane.getSelectionModel().select(tabMedicos);
        lblBreadcrumb.setText("Inicio > Médicos");
        actualizarEnlacesActivos(linkMedicos);
    }

    private void actualizarEnlacesActivos(Hyperlink activeLink) {
        Hyperlink[] links = {linkPacientes, linkMedicos};
        for (Hyperlink link : links) {
            if (link != null) {
                link.getStyleClass().remove("active");
            }
        }
        if (activeLink != null) {
            activeLink.getStyleClass().add("active");
        }
    }

    private void ajustarNavbar(double width) {
        if (width >= 960) {
            if (!headerTop.getChildren().contains(navLinks)) {
                headerMiddle.getChildren().clear();
                headerMiddle.setVisible(false);
                headerMiddle.setManaged(false);

                int idxSpacer1 = headerTop.getChildren().indexOf(spacer1);
                if (idxSpacer1 >= 0) {
                    headerTop.getChildren().add(idxSpacer1 + 1, navLinks);
                } else {
                    headerTop.getChildren().add(1, navLinks);
                }
            }

            spacer1.setVisible(true);
            spacer1.setManaged(true);
            spacer2.setVisible(true);
            spacer2.setManaged(true);

            if (width >= 1150) {
                navLinks.setHgap(20);
            } else {
                navLinks.setHgap(10);
            }
        } else {
            if (!headerMiddle.getChildren().contains(navLinks)) {
                headerTop.getChildren().remove(navLinks);
                headerMiddle.getChildren().add(navLinks);
                headerMiddle.setVisible(true);
                headerMiddle.setManaged(true);
            }

            spacer1.setVisible(true);
            spacer1.setManaged(true);
            spacer2.setVisible(false);
            spacer2.setManaged(false);

            navLinks.setHgap(12);
        }
     }

    /**
     * Gestiona el cierre de sesión del usuario.
     * Limpia el estado del SesionManager y retorna al usuario a la pantalla de login,
     * reemplazando el contenido de la ventana actual.
     */
    @FXML
    private void onUserProfileClick() {
        try {
            SesionManager.getInstance().cerrarSesion();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/view/login.fxml"));
            StackPane root = loader.load();
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/css/hopecare.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("HopeCare - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
