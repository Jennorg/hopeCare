package com.esperanza.hopecare.modules.pacientes_medicos.view;

import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.time.LocalDate;

public class PacienteFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumento;
    @FXML private DatePicker dpFechaNac;
    @FXML private ComboBox<String> cbGenero;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtHistoria;
    @FXML private ComboBox<String> cbGrupoSangre;
    @FXML private TextField txtAlergias;
    @FXML private TextField txtContacto;

    private Paciente pacienteActual;

    @FXML
    public void initialize() {
        cbGenero.setItems(FXCollections.observableArrayList("MASCULINO", "FEMENINO", "OTRO"));
        cbGrupoSangre.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
    }

    public void cargarPaciente(Paciente p) {
        this.pacienteActual = p;
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        txtDocumento.setText(p.getDocumentoIdentidad());
        
        if (p.getFechaNacimiento() != null && !p.getFechaNacimiento().trim().isEmpty()) {
            try {
                dpFechaNac.setValue(LocalDate.parse(p.getFechaNacimiento()));
            } catch (Exception e) {
                dpFechaNac.setValue(null);
            }
        } else {
            dpFechaNac.setValue(null);
        }

        cbGenero.setValue(p.getGenero());
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        txtDireccion.setText(p.getDireccion());
        txtHistoria.setText(p.getHistoriaClinica());
        cbGrupoSangre.setValue(p.getGrupoSanguineo());
        txtAlergias.setText(p.getAlergias());
        txtContacto.setText(p.getContactoEmergencia());
    }

    public boolean validar() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) return false;
        if (txtApellido.getText() == null || txtApellido.getText().trim().isEmpty()) return false;
        if (txtDocumento.getText() == null || txtDocumento.getText().trim().isEmpty()) return false;
        if (txtHistoria.getText() == null || txtHistoria.getText().trim().isEmpty()) return false;
        return true;
    }

    public Paciente obtenerPacienteModificado() {
        Paciente p = this.pacienteActual;
        if (p == null) {
            p = new Paciente();
        }
        p.setNombre(txtNombre.getText().trim());
        p.setApellido(txtApellido.getText().trim());
        p.setDocumentoIdentidad(txtDocumento.getText().trim());
        
        if (dpFechaNac.getValue() != null) {
            p.setFechaNacimiento(dpFechaNac.getValue().toString());
        } else {
            p.setFechaNacimiento("");
        }

        p.setGenero(cbGenero.getValue());
        p.setTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : "");
        p.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");
        p.setDireccion(txtDireccion.getText() != null ? txtDireccion.getText().trim() : "");
        p.setHistoriaClinica(txtHistoria.getText().trim());
        p.setGrupoSanguineo(cbGrupoSangre.getValue());
        p.setAlergias(txtAlergias.getText() != null ? txtAlergias.getText().trim() : "");
        p.setContactoEmergencia(txtContacto.getText() != null ? txtContacto.getText().trim() : "");
        return p;
    }
}
