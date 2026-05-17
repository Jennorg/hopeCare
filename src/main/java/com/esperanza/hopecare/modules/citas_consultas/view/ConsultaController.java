package com.esperanza.hopecare.modules.citas_consultas.view;

import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.presenter.ConsultaPresenter;
import com.esperanza.hopecare.modules.citas_consultas.view.IConsultaView;
import com.esperanza.hopecare.modules.medicamentos_lab.model.ExamenLaboratorio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultaController implements IConsultaView {
    @FXML private ComboBox<String> cbCitasPendientes;
    @FXML private TextArea txtSintomas, txtDiagnostico, txtTratamiento;
    @FXML private TextField txtPrecio;
    @FXML private Button btnCargar, btnGuardar, btnSolicitarExamen;

    private ConsultaPresenter presenter;
    private ObservableList<String> citasList;

    @FXML
    public void initialize() {
        presenter = new ConsultaPresenter(this);
        citasList = FXCollections.observableArrayList();
        cbCitasPendientes.setItems(citasList);

        btnCargar.setOnAction(e -> presenter.seleccionarCita());
        btnGuardar.setOnAction(e -> presenter.registrarConsulta());
        btnSolicitarExamen.setOnAction(e -> presenter.solicitarExamen());

        presenter.cargarCitasPendientes();
    }

    @Override
    public int getIdCitaSeleccionada() {
        String selected = cbCitasPendientes.getValue();
        if (selected == null || selected.isEmpty()) return -1;
        try {
            return Integer.parseInt(selected.split(" - ")[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String getDiagnostico() {
        return txtDiagnostico.getText();
    }

    @Override
    public String getSintomas() {
        return txtSintomas.getText();
    }

    @Override
    public String getTratamiento() {
        return txtTratamiento.getText();
    }

    @Override
    public double getPrecio() {
        try {
            return Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public void mostrarCitasPendientes(List<Cita> citas) {
        citasList.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Cita c : citas) {
            String texto = c.getIdCita() + " - Paciente: " + c.getPacienteNombre() +
                           " | Médico: " + c.getMedicoNombre() +
                           " | " + c.getFechaHora().format(formatter);
            citasList.add(texto);
        }
        if (citasList.isEmpty()) {
            cbCitasPendientes.setPromptText("No hay citas pendientes");
        } else {
            cbCitasPendientes.getSelectionModel().selectFirst();
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @Override
    public void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @Override
    public void limpiarFormulario() {
        txtSintomas.clear();
        txtDiagnostico.clear();
        txtTratamiento.clear();
        txtPrecio.clear();
    }

    @Override
    public void limpiarSeleccionCita() {
        cbCitasPendientes.getSelectionModel().clearSelection();
    }

    @Override
    public void actualizarEstadoAcciones(boolean consultaGuardada) {
        btnGuardar.setDisable(consultaGuardada);
        btnSolicitarExamen.setDisable(!consultaGuardada);
    }

    @Override
    public Integer solicitarExamen(List<ExamenLaboratorio> examenesDisponibles) {
        ComboBox<ExamenLaboratorio> cbExamenes = new ComboBox<>();
        cbExamenes.setPrefWidth(350);
        cbExamenes.getItems().setAll(examenesDisponibles);
        cbExamenes.setCellFactory(lv -> new ListCell<ExamenLaboratorio>() {
            @Override
            protected void updateItem(ExamenLaboratorio item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreExamen() : null);
            }
        });
        cbExamenes.setButtonCell(new ListCell<ExamenLaboratorio>() {
            @Override
            protected void updateItem(ExamenLaboratorio item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreExamen() : "Seleccionar examen...");
            }
        });

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Solicitar examen");
        dialog.setHeaderText("Seleccione el examen de laboratorio");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        VBox content = new VBox(10, new Label("Examen:"), cbExamenes);
        content.setStyle("-fx-padding: 15;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                ExamenLaboratorio seleccion = cbExamenes.getValue();
                return seleccion != null ? seleccion.getIdExamen() : null;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
}
