package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.session.SesionManager;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.MedicoDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private TabPane innerTabPane;
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
            if ("MEDICO".equals(rol) && innerTabPane != null) {
                innerTabPane.getSelectionModel().select(1);
                lblBreadcrumb.setText("Inicio > Registrar Consulta");
            } else {
                lblBreadcrumb.setText("Inicio > Citas Médicas");
            }
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
        String rol = SesionManager.getInstance().getRol();
        if ("MEDICO".equals(rol) && innerTabPane != null) {
            innerTabPane.getSelectionModel().select(1);
            lblBreadcrumb.setText("Inicio > Registrar Consulta");
        } else {
            lblBreadcrumb.setText("Inicio > Citas Médicas");
        }
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
        SesionManager sesion = SesionManager.getInstance();
        String rol = sesion.getRol();
        if ("MEDICO".equals(rol)) {
            abrirPerfilMedico();
        } else if ("PACIENTE".equals(rol)) {
            abrirPerfilPaciente();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cerrar Sesión");
            alert.setHeaderText("¿Está seguro de que desea cerrar la sesión actual?");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());
            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cerrarSesion();
            }
        }
    }

    private void abrirPerfilMedico() {
        SesionManager sesion = SesionManager.getInstance();
        MedicoDAO medicoDAO = new MedicoDAO();
        int idMedico = medicoDAO.obtenerIdMedicoPorIdPersona(sesion.getIdPersona());
        if (idMedico <= 0) return;

        double precioActual = medicoDAO.obtenerPrecioConsulta(idMedico);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Perfil del Médico");
        dialog.setHeaderText(sesion.getNombrePersona());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        TextField txtPrecio = new TextField(String.format("%.0f", precioActual));
        txtPrecio.setPromptText("Precio de consulta ($)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Precio de consulta ($):"), 0, 0);
        grid.add(txtPrecio, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double nuevoPrecio = Double.parseDouble(txtPrecio.getText().trim());
                    if (nuevoPrecio < 0) throw new NumberFormatException();
                    medicoDAO.actualizarPrecioConsulta(idMedico, nuevoPrecio);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Perfil");
                    alert.setHeaderText(null);
                    alert.setContentText("Precio de consulta actualizado a $" + String.format("%.0f", nuevoPrecio));
                    alert.showAndWait();
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Ingrese un precio válido.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void abrirPerfilPaciente() {
        SesionManager sesion = SesionManager.getInstance();
        PacienteDAO dao = new PacienteDAO();
        Paciente p = dao.obtenerPorIdPersona(sesion.getIdPersona());
        if (p == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Mi Perfil");
        dialog.setHeaderText(p.getNombreCompleto());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        String na = "—";
        grid.add(new Label("Documento:"), 0, 0);
        grid.add(new Label(nonNull(p.getDocumentoIdentidad())), 1, 0);
        grid.add(new Label("Fecha Nacimiento:"), 0, 1);
        grid.add(new Label(nonNull(p.getFechaNacimiento())), 1, 1);
        grid.add(new Label("Género:"), 0, 2);
        grid.add(new Label(nonNull(p.getGenero())), 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(new Label(nonNull(p.getTelefono())), 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(new Label(nonNull(p.getEmail())), 1, 4);
        grid.add(new Label("Dirección:"), 0, 5);
        grid.add(new Label(nonNull(p.getDireccion())), 1, 5);
        grid.add(new Label("Historia Clínica:"), 0, 6);
        grid.add(new Label(nonNull(p.getHistoriaClinica())), 1, 6);
        grid.add(new Label("Alergias:"), 0, 7);
        grid.add(new Label(nonNull(p.getAlergias())), 1, 7);
        grid.add(new Label("Grupo Sanguíneo:"), 0, 8);
        grid.add(new Label(nonNull(p.getGrupoSanguineo())), 1, 8);
        grid.add(new Label("Contacto Emergencia:"), 0, 9);
        grid.add(new Label(nonNull(p.getContactoEmergencia())), 1, 9);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private static String nonNull(String s) {
        return s != null && !s.trim().isEmpty() ? s : "—";
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
