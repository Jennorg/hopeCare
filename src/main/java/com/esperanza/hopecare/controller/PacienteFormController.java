package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.model.Paciente;
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

    private String mensajeError = "";

    public String getMensajeError() {
        return mensajeError;
    }

    public boolean validar() {
        mensajeError = "";

        // 1. Validar campos obligatorios
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mensajeError = "El nombre es obligatorio.";
            return false;
        }
        if (txtApellido.getText() == null || txtApellido.getText().trim().isEmpty()) {
            mensajeError = "El apellido es obligatorio.";
            return false;
        }
        if (txtDocumento.getText() == null || txtDocumento.getText().trim().isEmpty()) {
            mensajeError = "La cédula (documento de identidad) es obligatoria.";
            return false;
        }
        if (txtHistoria.getText() == null || txtHistoria.getText().trim().isEmpty()) {
            mensajeError = "El número de historia clínica es obligatorio.";
            return false;
        }

        // 2. Validar tipos/formatos y longitud de caracteres
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String documento = txtDocumento.getText().trim();
        String historia = txtHistoria.getText().trim();

        if (nombre.length() > 50) {
            mensajeError = "El nombre no puede tener más de 50 caracteres.";
            return false;
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) {
            mensajeError = "El nombre solo puede contener letras y espacios.";
            return false;
        }

        if (apellido.length() > 50) {
            mensajeError = "El apellido no puede tener más de 50 caracteres.";
            return false;
        }
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")) {
            mensajeError = "El apellido solo puede contener letras y espacios.";
            return false;
        }

        if (documento.length() < 5 || documento.length() > 20) {
            mensajeError = "La cédula debe tener entre 5 y 20 caracteres.";
            return false;
        }
        if (!documento.matches("^[0-9\\-]+$")) {
            mensajeError = "La cédula solo puede contener números y guiones.";
            return false;
        }

        if (historia.length() > 50) {
            mensajeError = "El número de historia clínica no puede superar los 50 caracteres.";
            return false;
        }
        if (!historia.matches("^[a-zA-Z0-9\\-]+$")) {
            mensajeError = "La historia clínica solo puede contener letras, números y guiones.";
            return false;
        }

        // 3. Validaciones de campos opcionales
        if (dpFechaNac.getValue() != null) {
            if (dpFechaNac.getValue().isAfter(LocalDate.now())) {
                mensajeError = "La fecha de nacimiento no puede ser una fecha futura.";
                return false;
            }
        }

        if (txtTelefono.getText() != null && !txtTelefono.getText().trim().isEmpty()) {
            String telefono = txtTelefono.getText().trim();
            if (telefono.length() > 20) {
                mensajeError = "El teléfono no puede superar los 20 caracteres.";
                return false;
            }
            if (!telefono.matches("^\\+?[0-9\\s\\-]+$")) {
                mensajeError = "El teléfono solo puede contener números, espacios, guiones y opcionalmente comenzar con '+'.";
                return false;
            }
        }

        if (txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty()) {
            String email = txtEmail.getText().trim();
            if (email.length() > 100) {
                mensajeError = "El correo electrónico no puede superar los 100 caracteres.";
                return false;
            }
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                mensajeError = "El formato del correo electrónico no es válido.";
                return false;
            }
        }

        if (txtDireccion.getText() != null && !txtDireccion.getText().trim().isEmpty()) {
            if (txtDireccion.getText().trim().length() > 250) {
                mensajeError = "La dirección no puede superar los 250 caracteres.";
                return false;
            }
        }

        if (txtAlergias.getText() != null && !txtAlergias.getText().trim().isEmpty()) {
            if (txtAlergias.getText().trim().length() > 250) {
                mensajeError = "Las alergias no pueden superar los 250 caracteres.";
                return false;
            }
        }

        if (txtContacto.getText() != null && !txtContacto.getText().trim().isEmpty()) {
            if (txtContacto.getText().trim().length() > 150) {
                mensajeError = "El contacto de emergencia no puede superar los 150 caracteres.";
                return false;
            }
        }

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
            p.setFechaNacimiento(null);
        }

        p.setGenero(cbGenero.getValue() != null && !cbGenero.getValue().isEmpty() ? cbGenero.getValue() : null);
        p.setTelefono(txtTelefono.getText() != null && !txtTelefono.getText().trim().isEmpty() ? txtTelefono.getText().trim() : null);
        p.setEmail(txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty() ? txtEmail.getText().trim() : null);
        p.setDireccion(txtDireccion.getText() != null && !txtDireccion.getText().trim().isEmpty() ? txtDireccion.getText().trim() : null);
        p.setHistoriaClinica(txtHistoria.getText().trim());
        p.setGrupoSanguineo(cbGrupoSangre.getValue() != null && !cbGrupoSangre.getValue().isEmpty() ? cbGrupoSangre.getValue() : null);
        p.setAlergias(txtAlergias.getText() != null && !txtAlergias.getText().trim().isEmpty() ? txtAlergias.getText().trim() : null);
        p.setContactoEmergencia(txtContacto.getText() != null && !txtContacto.getText().trim().isEmpty() ? txtContacto.getText().trim() : null);
        return p;
    }
}