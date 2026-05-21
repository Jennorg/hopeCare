package com.esperanza.hopecare.modules.citas_consultas.view;

import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.presenter.ConsultaPresenter;
import com.esperanza.hopecare.modules.citas_consultas.view.IConsultaView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultaController implements IConsultaView {
    @FXML private ComboBox<String> cbCitasPendientes;
    @FXML private TextArea txtSintomas, txtDiagnostico, txtTratamiento;
    @FXML private TextField txtPrecio;
    @FXML private Button btnCargar, btnGuardar;

    private ConsultaPresenter presenter;
    private ObservableList<String> citasList;

    @FXML
    public void initialize() {
        presenter = new ConsultaPresenter(this);
        citasList = FXCollections.observableArrayList();
        cbCitasPendientes.setItems(citasList);

        btnCargar.setOnAction(e -> presenter.seleccionarCita());
        btnGuardar.setOnAction(e -> presenter.registrarConsulta());

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
    }
}
