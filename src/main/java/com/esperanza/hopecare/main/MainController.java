package com.esperanza.hopecare.main;

import com.esperanza.hopecare.modules.facturacion.view.FacturacionController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabRegistro;
    @FXML private Tab tabPacientes;
    @FXML private Tab tabMedicos;
    @FXML private Tab tabCitas;
    @FXML private Tab tabFarmacia;
    @FXML private Tab tabLaboratorio;
    @FXML private Tab tabFacturacion;
    @FXML private Label lblBreadcrumb;

    @FXML private Hyperlink linkDashboard;
    @FXML private Hyperlink linkPacientes;
    @FXML private Hyperlink linkMedicos;
    @FXML private Hyperlink linkCitas;
    @FXML private Hyperlink linkFarmacia;
    @FXML private Hyperlink linkLaboratorio;
    @FXML private Hyperlink linkFacturacion;

    @FXML private HBox headerTop;
    @FXML private FlowPane navLinks;
    @FXML private FlowPane headerMiddle;
    @FXML private Pane spacer1;
    @FXML private Pane spacer2;

    private FacturacionController facturacionController;

    @FXML
    public void initialize() {
        mainTabPane.getSelectionModel().select(tabFarmacia);
        actualizarEnlacesActivos(linkFarmacia);

        Node facturaRoot = tabFacturacion.getContent();
        if (facturaRoot != null) {
            Object ctrl = facturaRoot.getProperties().get("controller");
            if (ctrl instanceof FacturacionController) {
                facturacionController = (FacturacionController) ctrl;
            }
        }

        // Add listener for responsive window resizing
        mainTabPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0) {
                ajustarNavbar(newVal.doubleValue());
            }
        });
    }

    @FXML
    private void navigateToDashboard() {
        mainTabPane.getSelectionModel().select(tabDashboard);
        lblBreadcrumb.setText("Inicio > Dashboard");
        actualizarEnlacesActivos(linkDashboard);
    }

    @FXML
    private void navigateToRegistro() {
        mainTabPane.getSelectionModel().select(tabRegistro);
        lblBreadcrumb.setText("Inicio > Registro");
        actualizarEnlacesActivos(null);
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

    @FXML
    private void navigateToCitas() {
        mainTabPane.getSelectionModel().select(tabCitas);
        lblBreadcrumb.setText("Inicio > Citas Médicas");
        actualizarEnlacesActivos(linkCitas);
    }

    @FXML
    private void navigateToFarmacia() {
        mainTabPane.getSelectionModel().select(tabFarmacia);
        lblBreadcrumb.setText("Inicio > Farmacia");
        actualizarEnlacesActivos(linkFarmacia);
    }

    @FXML
    private void navigateToLaboratorio() {
        mainTabPane.getSelectionModel().select(tabLaboratorio);
        lblBreadcrumb.setText("Inicio > Laboratorio");
        actualizarEnlacesActivos(linkLaboratorio);
    }

    @FXML
    private void navigateToFacturacion() {
        mainTabPane.getSelectionModel().select(tabFacturacion);
        lblBreadcrumb.setText("Inicio > Facturación");
        actualizarEnlacesActivos(linkFacturacion);
        if (facturacionController != null) {
            facturacionController.refrescar();
        }
    }

    private void actualizarEnlacesActivos(Hyperlink activeLink) {
        Hyperlink[] links = {linkDashboard, linkPacientes, linkMedicos, linkCitas, linkFarmacia, linkLaboratorio, linkFacturacion};
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
            // Modo Wide / Medium: Nav links en la fila superior con los espaciadores
            if (!headerTop.getChildren().contains(navLinks)) {
                headerMiddle.getChildren().clear();
                headerMiddle.setVisible(false);
                headerMiddle.setManaged(false);

                // Insertar de nuevo navLinks en la fila superior entre spacer1 y spacer2
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
            // Modo Compacto: Nav links en fila propia (headerMiddle)
            if (!headerMiddle.getChildren().contains(navLinks)) {
                headerTop.getChildren().remove(navLinks);
                headerMiddle.getChildren().add(navLinks);
                headerMiddle.setVisible(true);
                headerMiddle.setManaged(true);
            }

            // Ocultar spacer2, mantener spacer1 para alinear Logo e info de Usuario
            spacer1.setVisible(true);
            spacer1.setManaged(true);
            spacer2.setVisible(false);
            spacer2.setManaged(false);

            navLinks.setHgap(12);
        }
    }
}