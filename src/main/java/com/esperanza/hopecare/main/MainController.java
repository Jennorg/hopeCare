package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.session.SesionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabCitas;
    @FXML private Tab tabMisCitas;
    @FXML private Label lblBreadcrumb;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    @FXML private Hyperlink linkCitas;
    @FXML private Hyperlink linkMisCitas;

    @FXML private HBox headerTop;
    @FXML private FlowPane navLinks;
    @FXML private FlowPane headerMiddle;
    @FXML private Pane spacer1;
    @FXML private Pane spacer2;

    @FXML
    public void initialize() {
        SesionManager sesion = SesionManager.getInstance();
        lblUserName.setText(sesion.getNombrePersona());
        lblUserRole.setText(sesion.getRol());

        String rol = sesion.getRol();

        if ("PACIENTE".equals(rol)) {
            mainTabPane.getTabs().remove(tabCitas);
            mainTabPane.getSelectionModel().select(tabMisCitas);
            lblBreadcrumb.setText("Inicio > Mis Citas");
            linkCitas.setVisible(false);
            linkCitas.setManaged(false);
        } else {
            mainTabPane.getTabs().remove(tabMisCitas);
            mainTabPane.getSelectionModel().select(tabCitas);
            lblBreadcrumb.setText("Inicio > Citas Médicas");
            linkMisCitas.setVisible(false);
            linkMisCitas.setManaged(false);
        }

        mainTabPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0) {
                ajustarNavbar(newVal.doubleValue());
            }
        });
    }

    @FXML
    private void onLogoutClick() {
        cerrarSesion();
    }

    @FXML
    private void navigateToCitas() {
        mainTabPane.getSelectionModel().select(tabCitas);
        lblBreadcrumb.setText("Inicio > Citas Médicas");
        actualizarEnlacesActivos(linkCitas);
    }

    @FXML
    private void navigateToMisCitas() {
        mainTabPane.getSelectionModel().select(tabMisCitas);
        lblBreadcrumb.setText("Inicio > Mis Citas");
        actualizarEnlacesActivos(linkMisCitas);
    }

    private void actualizarEnlacesActivos(Hyperlink activeLink) {
        if (linkCitas != null) linkCitas.getStyleClass().remove("active");
        if (linkMisCitas != null) linkMisCitas.getStyleClass().remove("active");
        if (activeLink != null) activeLink.getStyleClass().add("active");
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

    @FXML
    private void onUserProfileClick() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Está seguro de que desea cerrar la sesión actual?");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            cerrarSesion();
        }
    }

    private void cerrarSesion() {
        try {
            SesionManager.getInstance().cerrarSesion();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/esperanza/hopecare/modules/Auth/view/login.fxml")
            );
            javafx.scene.layout.BorderPane root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) mainTabPane.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1280, 720);
            scene.getStylesheets().add(
                getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("HopeCare – Sistema de Gestión Hospitalaria");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
