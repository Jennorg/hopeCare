package com.esperanza.hopecare.modules.pacientes_medicos.view;

import com.esperanza.hopecare.modules.pacientes_medicos.dao.EspecialidadDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Especialidad;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Medico;
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
        chkActivo.setSelected(m.getIdMedico() == 0 || m.isActivo());

        if (m.getIdEspecialidad() > 0) {
            for (Especialidad e : cbEspecialidad.getItems()) {
                if (e.getIdEspecialidad() == m.getIdEspecialidad()) {
                    cbEspecialidad.setValue(e);
                    break;
                }
            }
        }
    }

    public boolean validar() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) return false;
        if (txtApellido.getText() == null || txtApellido.getText().trim().isEmpty()) return false;
        if (txtDocumento.getText() == null || txtDocumento.getText().trim().isEmpty()) return false;
        if (cbEspecialidad.getValue() == null) return false;
        if (txtRegistro.getText() == null || txtRegistro.getText().trim().isEmpty()) return false;
        if (txtPrecio.getText() == null || txtPrecio.getText().trim().isEmpty()) return false;
        try {
            Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            return false;
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
            m.setFechaNacimiento("");
        }

        m.setGenero(cbGenero.getValue());
        m.setTelefono(txtTelefono.getText() != null ? txtTelefono.getText().trim() : "");
        m.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");
        m.setDireccion(txtDireccion.getText() != null ? txtDireccion.getText().trim() : "");
        
        Especialidad esp = cbEspecialidad.getValue();
        if (esp != null) {
            m.setIdEspecialidad(esp.getIdEspecialidad());
            m.setNombreEspecialidad(esp.getNombre());
        }

        m.setRegistroMedico(txtRegistro.getText().trim());
        m.setPrecioConsulta(Double.parseDouble(txtPrecio.getText().trim()));
        m.setActivo(chkActivo.isSelected());
        return m;
    }
}
