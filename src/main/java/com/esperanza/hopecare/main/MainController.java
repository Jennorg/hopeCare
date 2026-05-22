package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.session.SesionManager;
import com.esperanza.hopecare.modules.facturacion.view.FacturacionController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabFacturacion;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Hyperlink linkFacturacion;

    private FacturacionController facturacionController;

    @FXML
    public void initialize() {
        SesionManager sesion = SesionManager.getInstance();
        lblUserName.setText(sesion.getNombreUsuario());
        lblUserRole.setText(sesion.getNombreRol());

        mainTabPane.getSelectionModel().select(tabFacturacion);
        linkFacturacion.getStyleClass().add("active");

        Node facturaRoot = tabFacturacion.getContent();
        if (facturaRoot != null) {
            Object ctrl = facturaRoot.getProperties().get("controller");
            if (ctrl instanceof FacturacionController) {
                facturacionController = (FacturacionController) ctrl;
            }
        }
    }

    @FXML
    private void navigateToFacturacion() {
        mainTabPane.getSelectionModel().select(tabFacturacion);
        if (facturacionController != null) {
            facturacionController.refrescar();
        }
    }

    @FXML
    private void onUserProfileClick() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Está seguro de que desea cerrar la sesión actual?");
        alert.setContentText("Se guardarán los cambios pendientes.");
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
