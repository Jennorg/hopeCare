package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.dao.EspecialidadDAO;
import com.esperanza.hopecare.model.Especialidad;
import com.esperanza.hopecare.model.Medico;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class MedicoFormController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumento;
    @FXML private DatePicker dpFechaNac;
    @FXML private ComboBox<String> cbGenero;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<Especialidad> cbEspecialidad;
    @FXML private TextField txtRegistro;
    @FXML private TextField txtPrecio;
    @FXML private CheckBox chkActivo;

    private Medico medicoActual;
    private EspecialidadDAO especialidadDAO;

    @FXML
    public void initialize() {
        especialidadDAO = new EspecialidadDAO();
        cbGenero.setItems(FXCollections.observableArrayList("MASCULINO", "FEMENINO", "OTRO"));
        configurarComboEspecialidades();
    }

    private void configurarComboEspecialidades() {
        cbEspecialidad.setItems(FXCollections.observableArrayList(especialidadDAO.listarTodas()));
        cbEspecialidad.setCellFactory(lv -> new ListCell<Especialidad>() {
            @Override
            protected void updateItem(Especialidad item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombre() : null);
            }
        });
        cbEspecialidad.setButtonCell(new ListCell<Especialidad>() {
            @Override
            protected void updateItem(Especialidad item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombre() : "Seleccione especialidad...");
            }
        });
    }

    public void cargarMedico(Medico m) {
        this.medicoActual = m;
        txtNombre.setText(m.getNombre());
        txtApellido.setText(m.getApellido());
        txtDocumento.setText(m.getDocumentoIdentidad());
        
        if (m.getFechaNacimiento() != null && !m.getFechaNacimiento().trim().isEmpty()) {
            try {
                dpFechaNac.setValue(LocalDate.parse(m.getFechaNacimiento()));
            } catch (Exception e) {
                dpFechaNac.setValue(null);
            }
        } else {
            dpFechaNac.setValue(null);
        }

        cbGenero.setValue(m.getGenero());
        txtTelefono.setText(m.getTelefono());
        txtEmail.setText(m.getEmail());
        txtDireccion.setText(m.getDireccion());
        txtRegistro.setText(m.getRegistroMedico());
        txtPrecio.setText(m.getIdMedico() > 0 ? String.valueOf(m.getPrecioConsulta()) : "50.0");
        chkActivo.setSelected(m.getIdMedico() == 0 || m.getActivo() == 1);

        if (m.getIdEspecialidad() > 0) {
            for (Especialidad e : cbEspecialidad.getItems()) {
                if (e.getIdEspecialidad() == m.getIdEspecialidad()) {
                    cbEspecialidad.setValue(e);
                    break;
                }
            }
        }
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
        if (cbEspecialidad.getValue() == null) {
            mensajeError = "La especialidad es obligatoria.";
            return false;
        }
        if (txtRegistro.getText() == null || txtRegistro.getText().trim().isEmpty()) {
            mensajeError = "El número de registro médico es obligatorio.";
            return false;
        }
        if (txtPrecio.getText() == null || txtPrecio.getText().trim().isEmpty()) {
            mensajeError = "El precio de consulta es obligatorio.";
            return false;
        }
        
        // 2. Validar tipos/formatos y longitud de caracteres
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String documento = txtDocumento.getText().trim();
        String registro = txtRegistro.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        
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
        
        if (registro.length() > 50) {
            mensajeError = "El registro médico no puede superar los 50 caracteres.";
            return false;
        }
        if (!registro.matches("^[a-zA-Z0-9\\-]+$")) {
            mensajeError = "El registro médico solo puede contener letras, números y guiones.";
            return false;
        }
        
        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                mensajeError = "El precio de consulta debe ser un número positivo mayor que cero.";
                return false;
            }
        } catch (NumberFormatException e) {
            mensajeError = "El precio de consulta debe ser un número decimal válido.";
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
        
        return true;
    }

    public Medico obtenerMedicoModificado() {
        Medico m = this.medicoActual;
        if (m == null) {
            m = new Medico();
        }
        m.setNombre(txtNombre.getText().trim());
        m.setApellido(txtApellido.getText().trim());
        m.setDocumentoIdentidad(txtDocumento.getText().trim());
        
        if (dpFechaNac.getValue() != null) {
            m.setFechaNacimiento(dpFechaNac.getValue().toString());
        } else {
            m.setFechaNacimiento(null);
        }

        m.setGenero(cbGenero.getValue() != null && !cbGenero.getValue().isEmpty() ? cbGenero.getValue() : null);
        m.setTelefono(txtTelefono.getText() != null && !txtTelefono.getText().trim().isEmpty() ? txtTelefono.getText().trim() : null);
        m.setEmail(txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty() ? txtEmail.getText().trim() : null);
        m.setDireccion(txtDireccion.getText() != null && !txtDireccion.getText().trim().isEmpty() ? txtDireccion.getText().trim() : null);
        
        Especialidad esp = cbEspecialidad.getValue();
        if (esp != null) {
            m.setIdEspecialidad(esp.getIdEspecialidad());
            m.setNombreEspecialidad(esp.getNombre());
        }

        m.setRegistroMedico(txtRegistro.getText().trim());
        m.setPrecioConsulta(Double.parseDouble(txtPrecio.getText().trim()));
        m.setActivo(chkActivo.isSelected() ? 1 : 0);
        return m;
    }
}
