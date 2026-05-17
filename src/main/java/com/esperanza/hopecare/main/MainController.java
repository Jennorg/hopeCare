package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.session.SesionManager;
import com.esperanza.hopecare.facturacion.view.FacturacionController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabRegistro;
    @FXML private Tab tabCitas;
    @FXML private Tab tabFarmacia;
    @FXML private Tab tabLaboratorio;
    @FXML private Tab tabFacturacion;
    @FXML private Label lblBreadcrumb;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    private FacturacionController facturacionController;

    @FXML
    public void initialize() {
        SesionManager sesion = SesionManager.getInstance();
        lblUserName.setText(sesion.getNombreUsuario());
        lblUserRole.setText(sesion.getNombreRol());

        mainTabPane.getSelectionModel().select(tabFarmacia);

        Node facturaRoot = tabFacturacion.getContent();
        if (facturaRoot != null) {
            Object ctrl = facturaRoot.getProperties().get("controller");
            if (ctrl instanceof FacturacionController) {
                facturacionController = (FacturacionController) ctrl;
            }
        }
    }

    @FXML
    private void navigateToDashboard() {
        mainTabPane.getSelectionModel().select(tabDashboard);
        lblBreadcrumb.setText("Inicio > Dashboard");
    }

    @FXML
    private void navigateToRegistro() {
        mainTabPane.getSelectionModel().select(tabRegistro);
        lblBreadcrumb.setText("Inicio > Registro");
    }

    @FXML
    private void navigateToCitas() {
        mainTabPane.getSelectionModel().select(tabCitas);
        lblBreadcrumb.setText("Inicio > Citas Médicas");
    }

    @FXML
    private void navigateToFarmacia() {
        mainTabPane.getSelectionModel().select(tabFarmacia);
        lblBreadcrumb.setText("Inicio > Farmacia");
    }

    @FXML
    private void navigateToLaboratorio() {
        mainTabPane.getSelectionModel().select(tabLaboratorio);
        lblBreadcrumb.setText("Inicio > Laboratorio");
    }

    @FXML
    private void navigateToFacturacion() {
        mainTabPane.getSelectionModel().select(tabFacturacion);
        lblBreadcrumb.setText("Inicio > Facturación");
        if (facturacionController != null) {
            facturacionController.refrescar();
        }
    }
}