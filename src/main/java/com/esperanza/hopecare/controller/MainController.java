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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabPacientes;
    @FXML private Tab tabMedicos;
    @FXML private Tab tabCitas;
    @FXML private Tab tabConsultas;
    @FXML private Tab tabFacturacion;
    
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblBreadcrumb;

    @FXML private Hyperlink linkDashboard;
    @FXML private Hyperlink linkPacientes;
    @FXML private Hyperlink linkMedicos;
    @FXML private Hyperlink linkCitas;
    @FXML private Hyperlink linkConsultas;
    @FXML private Hyperlink linkFacturacion;

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

        mainTabPane.getSelectionModel().select(tabDashboard);
        actualizarEnlacesActivos(linkDashboard);
        lblBreadcrumb.setText("Inicio > Dashboard");

        // Add listener for responsive window resizing
        mainTabPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > 0) {
                ajustarNavbar(newVal.doubleValue());
            }
        });
        
        aplicarPermisos();
    }

    private void aplicarPermisos() {
        SesionManager sesion = SesionManager.getInstance();
        boolean isAdmin = sesion.isAdmin();
        boolean isRecepcionista = sesion.isRecepcionista();
        boolean isMedico = sesion.isMedico();
        
        if (!isAdmin && !isRecepcionista && !isMedico) {
            linkDashboard.setVisible(false); linkDashboard.setManaged(false);
            linkPacientes.setVisible(false); linkPacientes.setManaged(false);
            linkMedicos.setVisible(false); linkMedicos.setManaged(false);
            linkFacturacion.setVisible(false); linkFacturacion.setManaged(false);
            
            mainTabPane.getSelectionModel().select(tabCitas);
            actualizarEnlacesActivos(linkCitas);
            lblBreadcrumb.setText("Inicio > Citas");
        } else if (isRecepcionista || isMedico) {
            linkDashboard.setVisible(false); linkDashboard.setManaged(false);
            
            if (isRecepcionista) {
                // Receptionist can see Billing but not Dashboard
                mainTabPane.getSelectionModel().select(tabCitas);
                actualizarEnlacesActivos(linkCitas);
                lblBreadcrumb.setText("Inicio > Citas");
            } else if (isMedico) {
                linkFacturacion.setVisible(false); linkFacturacion.setManaged(false);
                mainTabPane.getSelectionModel().select(tabPacientes);
                actualizarEnlacesActivos(linkPacientes);
                lblBreadcrumb.setText("Inicio > Pacientes");
            }
        }
    }

    @FXML
    private void navigateToDashboard() {
        mainTabPane.getSelectionModel().select(tabDashboard);
        lblBreadcrumb.setText("Inicio > Dashboard");
        actualizarEnlacesActivos(linkDashboard);
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
        lblBreadcrumb.setText("Inicio > Citas");
        actualizarEnlacesActivos(linkCitas);
    }

    @FXML
    private void navigateToConsultas() {
        mainTabPane.getSelectionModel().select(tabConsultas);
        lblBreadcrumb.setText("Inicio > Consultas");
        actualizarEnlacesActivos(linkConsultas);
    }

    @FXML
    private void navigateToFacturacion() {
        mainTabPane.getSelectionModel().select(tabFacturacion);
        lblBreadcrumb.setText("Inicio > Facturación");
        actualizarEnlacesActivos(linkFacturacion);
    }

    @FXML
    private void onCerrarSesion() {
        try {
            SesionManager.getInstance().cerrarSesion();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/view/login.fxml"));
            Parent root = loader.load();
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

    private void actualizarEnlacesActivos(Hyperlink activeLink) {
        Hyperlink[] links = {linkDashboard, linkPacientes, linkMedicos, linkCitas, linkConsultas, linkFacturacion};
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
        if (width >= 1000) {
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
        }
     }
}
