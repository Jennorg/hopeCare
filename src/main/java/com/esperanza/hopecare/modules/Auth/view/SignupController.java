package com.esperanza.hopecare.modules.Auth.view;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.Auth.dto.RegistroDTO;
import com.esperanza.hopecare.modules.Auth.service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    @FXML private Label lblStep1;
    @FXML private Label lblStep2;
    @FXML private Label lblStep3;

    @FXML private VBox paso1;
    @FXML private VBox paso2;
    @FXML private VBox paso3;

    @FXML private TextField        txtUsuario;
    @FXML private PasswordField    txtContrasena;
    @FXML private PasswordField    txtConfirmar;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Label            lblMensaje1;

    @FXML private TextField        txtNombre;
    @FXML private TextField        txtApellido;
    @FXML private TextField        txtDocumento;
    @FXML private DatePicker       dpFechaNac;
    @FXML private TextField        txtTelefono;
    @FXML private ComboBox<String> cmbGenero;
    @FXML private TextField        txtEmail;
    @FXML private TextField        txtDireccion;
    @FXML private Label            lblMensaje2;

    @FXML private ComboBox<String> cmbEspecialidad;
    @FXML private TextField        txtRegistroMedico;
    @FXML private Label            lblMensaje3;

    private int pasoActual = 1;
    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbRol.setItems(FXCollections.observableArrayList(
                "ADMINISTRADOR", "MEDICO"
        ));
        cmbRol.getSelectionModel().selectFirst();

        cmbGenero.setItems(FXCollections.observableArrayList(
                "M – Masculino", "F – Femenino", "O – Otro"
        ));
        cmbGenero.getSelectionModel().selectFirst();

        cmbEspecialidad.setItems(FXCollections.observableArrayList(cargarEspecialidades()));
        if (!cmbEspecialidad.getItems().isEmpty()) {
            cmbEspecialidad.getSelectionModel().selectFirst();
        }

        cmbRol.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (pasoActual != 1) {
                mostrarPaso(1);
            }
            actualizarVisibilidadPaso3();
            actualizarStepsBar(pasoActual);
        });

        mostrarPaso(1);
    }

    private List<String> cargarEspecialidades() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_especialidad FROM especialidad ORDER BY nombre_especialidad";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("nombre_especialidad"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lista.isEmpty()) {
            lista.add("Otra especialidad");
        }
        return lista;
    }

    @FXML
    private void irAPaso2() {
        if (!validarPaso1()) return;
        mostrarPaso(2);
    }

    @FXML
    private void irAPaso1() {
        mostrarPaso(1);
    }

    @FXML
    private void irAPaso3OrFinalizar() {
        if (!validarPaso2()) return;

        String rol = cmbRol.getValue();
        if ("MEDICO".equals(rol)) {
            mostrarPaso(3);
        } else {
            finalizarRegistro();
        }
    }

    private void actualizarVisibilidadPaso3() {
        boolean esMedico = "MEDICO".equals(cmbRol.getValue());
        lblStep3.setVisible(esMedico);
        lblStep3.setManaged(esMedico);
    }

    private void mostrarPaso(int paso) {
        pasoActual = paso;
        boolean esMedico = "MEDICO".equals(cmbRol.getValue());

        paso1.setVisible(paso == 1);
        paso1.setManaged(paso == 1);

        paso2.setVisible(paso == 2);
        paso2.setManaged(paso == 2);

        paso3.setVisible(paso == 3);
        paso3.setManaged(paso == 3 && esMedico);

        lblStep3.setVisible(esMedico);
        lblStep3.setManaged(esMedico);

        actualizarStepsBar(paso);

        clearError(lblMensaje1);
        clearError(lblMensaje2);
        clearError(lblMensaje3);
    }

    private void setVisible(VBox panel, boolean visible) {
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    @FXML
    private void finalizarRegistro() {
        if (pasoActual == 3 && !validarPaso3()) return;

        RegistroDTO dto = new RegistroDTO();
        dto.setNombreUsuario(getUsuario());
        dto.setContrasena(getContrasena());
        dto.setRol(getRol());
        dto.setNombre(getNombre());
        dto.setApellido(getApellido());
        dto.setDocumento(getDocumento());
        dto.setFechaNacimiento(getFechaNac());
        dto.setTelefono(getTelefono());
        dto.setGenero(getGenero());
        dto.setEmail(getEmail());
        dto.setDireccion(getDireccion());
        dto.setEspecialidad(getEspecialidad());
        dto.setRegistroMedico(getRegistroMedico());

        String error = authService.registrar(dto);

        if (error != null) {
            if (error.contains("usuario ya está en uso")) {
                setError(lblMensaje1, error);
                mostrarPaso(1);
            } else {
                setError(lblMensaje3, error);
            }
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro completado");
        alert.setHeaderText(null);
        alert.setContentText("Cuenta registrada exitosamente.\nYa puedes iniciar sesión.");
        alert.showAndWait();

        goToLogin();
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/esperanza/hopecare/modules/Auth/view/login.fxml")
            );
            BorderPane root = loader.load();

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            double x = stage.getX();
            double y = stage.getY();
            double w = stage.getWidth();
            double h = stage.getHeight();
            boolean max = stage.isMaximized();

            Scene scene = new Scene(root, 900, 660);
            scene.getStylesheets().add(
                getClass().getResource("/com/esperanza/hopecare/main/hopecare.css")
                          .toExternalForm()
            );
            stage.setTitle("HopeCare – Iniciar Sesión");
            stage.setScene(scene);
            stage.setResizable(true);

            if (max) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            } else {
                stage.setMaximized(false);
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(w);
                stage.setHeight(h);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarStepsBar(int pasoActivo) {
        boolean esMedico = "MEDICO".equals(cmbRol.getValue());
        Label[] labels = { lblStep1, lblStep2, lblStep3 };
        for (int i = 0; i < labels.length; i++) {
            labels[i].getStyleClass().removeAll("step-active", "step-done", "step-inactive");
            if (!esMedico && i == 2) {
                continue;
            }
            if (i + 1 == pasoActivo) {
                labels[i].getStyleClass().add("step-active");
            } else if (i + 1 < pasoActivo) {
                labels[i].getStyleClass().add("step-done");
            } else {
                labels[i].getStyleClass().add("step-inactive");
            }
        }
    }

    private boolean validarPaso1() {
        String usuario = txtUsuario.getText().trim();
        String pass    = txtContrasena.getText();
        String confirm = txtConfirmar.getText();
        String rol     = cmbRol.getValue();

        if (usuario.isEmpty() || pass.isEmpty() || confirm.isEmpty() || rol == null) {
            setError(lblMensaje1, "Todos los campos son obligatorios.");
            return false;
        }
        if (usuario.length() < 3) {
            setError(lblMensaje1, "El usuario debe tener al menos 3 caracteres.");
            return false;
        }
        if (pass.length() < 6) {
            setError(lblMensaje1, "La contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        if (!pass.equals(confirm)) {
            setError(lblMensaje1, "Las contraseñas no coinciden.");
            return false;
        }
        if (authService.usuarioExiste(usuario)) {
            setError(lblMensaje1, "El nombre de usuario ya está registrado.");
            return false;
        }
        return true;
    }

    private boolean validarPaso2() {
        String nombre   = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String documento = txtDocumento.getText().trim();
        String telefono  = txtTelefono.getText().trim();
        String email     = txtEmail.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            setError(lblMensaje2, "Nombre y apellido son obligatorios.");
            return false;
        }
        if (!documento.isEmpty()) {
            if (!documento.matches("\\d+")) {
                setError(lblMensaje2, "La cédula debe contener solo números.");
                return false;
            }
            if (documento.length() < 5 || documento.length() > 20) {
                setError(lblMensaje2, "La cédula debe tener entre 5 y 20 dígitos.");
                return false;
            }
            if (authService.documentoExiste(documento)) {
                setError(lblMensaje2, "Esta cédula ya está registrada.");
                return false;
            }
        }
        if (!telefono.isEmpty()) {
            if (!telefono.matches("\\d+")) {
                setError(lblMensaje2, "El teléfono debe contener solo números.");
                return false;
            }
            if (telefono.length() < 7 || telefono.length() > 15) {
                setError(lblMensaje2, "El teléfono debe tener entre 7 y 15 dígitos.");
                return false;
            }
        }
        if (!email.isEmpty()) {
            if (!email.matches("^[\\w.%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                setError(lblMensaje2, "El correo electrónico no tiene un formato válido.");
                return false;
            }
            if (authService.emailExiste(email)) {
                setError(lblMensaje2, "Este correo electrónico ya está registrado.");
                return false;
            }
        }
        LocalDate fecha = dpFechaNac.getValue();
        if (fecha != null && fecha.isAfter(LocalDate.now())) {
            setError(lblMensaje2, "La fecha de nacimiento no puede ser futura.");
            return false;
        }
        return true;
    }

    private boolean validarPaso3() {
        String registro = txtRegistroMedico.getText().trim();
        if (registro.isEmpty()) {
            setError(lblMensaje3, "El número de registro médico es obligatorio.");
            return false;
        }
        if (registro.length() < 4) {
            setError(lblMensaje3, "El registro médico parece demasiado corto.");
            return false;
        }
        if (cmbEspecialidad.getValue() == null) {
            setError(lblMensaje3, "Debes seleccionar una especialidad.");
            return false;
        }
        if (authService.registroMedicoExiste(registro)) {
            setError(lblMensaje3, "Este registro médico ya está registrado.");
            return false;
        }
        return true;
    }

    private void setError(Label lbl, String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            clearError(lbl);
        } else {
            lbl.setText(mensaje);
            lbl.setStyle(""); // Clear legacy direct style overrides
            if (!lbl.getStyleClass().contains("msg-error-active")) {
                lbl.getStyleClass().add("msg-error-active");
            }
            lbl.setVisible(true);
            lbl.setManaged(true);
        }
    }

    private void clearError(Label lbl) {
        lbl.setText("");
        lbl.getStyleClass().remove("msg-error-active");
        lbl.setVisible(false);
        lbl.setManaged(false);
    }

    public String getUsuario()        { return txtUsuario.getText().trim(); }
    public String getContrasena()     { return txtContrasena.getText(); }
    public String getRol()            { return cmbRol.getValue(); }
    public String getNombre()         { return txtNombre.getText().trim(); }
    public String getApellido()       { return txtApellido.getText().trim(); }
    public String getDocumento()      { return txtDocumento.getText().trim(); }
    public LocalDate getFechaNac()    { return dpFechaNac.getValue(); }
    public String getTelefono()       { return txtTelefono.getText().trim(); }
    public String getGenero() {
        String g = cmbGenero.getValue();
        return (g != null) ? g.substring(0, 1) : null;
    }
    public String getEmail()          { return txtEmail.getText().trim(); }
    public String getDireccion()      { return txtDireccion.getText().trim(); }
    public String getEspecialidad()   { return cmbEspecialidad.getValue(); }
    public String getRegistroMedico() { return txtRegistroMedico.getText().trim(); }
}
