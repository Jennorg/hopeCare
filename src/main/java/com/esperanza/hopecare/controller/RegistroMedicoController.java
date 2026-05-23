package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.dao.MedicoDAO;
import com.esperanza.hopecare.model.Medico;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class RegistroMedicoController {
    @FXML private TextField txtDocumento;
    @FXML private ComboBox<String> cbEspecialidad;
    @FXML private TextField txtRegistro;

    private MedicoDAO medicoDAO;

    @FXML
    public void initialize() {
        medicoDAO = new MedicoDAO();
        cbEspecialidad.getItems().addAll("Medicina General", "Pediatría", "Traumatología");
    }

    @FXML
    private void btnRegistrarClick() {
        String documento = txtDocumento.getText().trim();
        String especialidad = cbEspecialidad.getValue();
        String registro = txtRegistro.getText().trim();

        if (documento.isEmpty() || especialidad == null || registro.isEmpty()) {
            mostrarAlerta("Error", "Complete todos los campos", Alert.AlertType.ERROR);
            return;
        }

        // Convertir especialidad a ID (1,2,3)
        int idEspecialidad = cbEspecialidad.getSelectionModel().getSelectedIndex() + 1;
        Medico medico = new Medico(documento, idEspecialidad, registro);

        boolean ok = medicoDAO.insertarMedico(medico);
        if (ok) {
            mostrarAlerta("Éxito", "Médico registrado con ID Persona: " + medico.getIdPersona(), Alert.AlertType.INFORMATION);
            limpiarCampos();
        } else {
            mostrarAlerta("Error", "No se pudo registrar el médico", Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        txtDocumento.clear();
        cbEspecialidad.getSelectionModel().clearSelection();
        txtRegistro.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
