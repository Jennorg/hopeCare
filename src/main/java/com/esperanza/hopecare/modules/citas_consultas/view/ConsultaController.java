package com.esperanza.hopecare.modules.citas_consultas.view;

import com.esperanza.hopecare.common.session.SesionManager;
import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.model.Consulta;
import com.esperanza.hopecare.modules.citas_consultas.presenter.ConsultaPresenter;
import com.esperanza.hopecare.modules.citas_consultas.view.IConsultaView;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.MedicoDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultaController implements IConsultaView {
    @FXML private TableView<Cita> tvConsultas;
    @FXML private Button btnNuevaConsulta;

    private ConsultaPresenter presenter;
    private ObservableList<Cita> consultasList;
    private String rol;
    private int idMedicoLogueado = -1;

    @FXML
    public void initialize() {
        presenter = new ConsultaPresenter(this);

        SesionManager sesion = SesionManager.getInstance();
        rol = sesion.getRol();
        if ("MEDICO".equals(rol)) {
            idMedicoLogueado = new MedicoDAO().obtenerIdMedicoPorIdPersona(sesion.getIdPersona());
        }

        configurarTablaHistorial();
        btnNuevaConsulta.setOnAction(e -> abrirDialogoNuevaConsulta());

        cargarHistorial();
    }

    private void configurarTablaHistorial() {
        TableColumn<Cita, String> colId = (TableColumn<Cita, String>) tvConsultas.getColumns().get(0);
        TableColumn<Cita, String> colPac = (TableColumn<Cita, String>) tvConsultas.getColumns().get(1);
        TableColumn<Cita, String> colMed = (TableColumn<Cita, String>) tvConsultas.getColumns().get(2);
        TableColumn<Cita, String> colFecha = (TableColumn<Cita, String>) tvConsultas.getColumns().get(3);
        TableColumn<Cita, String> colDiag = (TableColumn<Cita, String>) tvConsultas.getColumns().get(4);
        TableColumn<Cita, String> colPrecio = (TableColumn<Cita, String>) tvConsultas.getColumns().get(5);

        colId.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getConsultaId())));
        colPac.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colMed.setCellValueFactory(new PropertyValueFactory<>("medicoNombre"));
        colFecha.setCellValueFactory(cd -> {
            if (cd.getValue().getConsultaFecha() != null) {
                return new SimpleStringProperty(cd.getValue().getConsultaFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("");
        });
        colDiag.setCellValueFactory(new PropertyValueFactory<>("consultaDiagnostico"));
        colPrecio.setCellValueFactory(cd -> {
            double p = cd.getValue().getPrecio();
            return new SimpleStringProperty(p > 0 ? String.format("$%.2f", p) : "—");
        });

        consultasList = FXCollections.observableArrayList();
        tvConsultas.setItems(consultasList);
    }

    private void cargarHistorial() {
        CitaDAO dao = new CitaDAO();
        List<Cita> list;
        if ("MEDICO".equals(rol) && idMedicoLogueado > 0) {
            list = dao.listarConsultasAtendidasPorMedico(idMedicoLogueado);
        } else {
            list = dao.listarConsultasAtendidas();
        }
        consultasList.setAll(list);
    }

    private void abrirDialogoNuevaConsulta() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva Consulta");
        dialog.setHeaderText("Seleccione una cita programada y registre la consulta");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        CitaDAO citaDAO = new CitaDAO();
        ConsultaDAO consultaDAO = new ConsultaDAO();

        List<Cita> pendientes = citaDAO.obtenerCitasPorEstadoConNombres("PROGRAMADA");
        if ("MEDICO".equals(rol) && idMedicoLogueado > 0) {
            pendientes.removeIf(c -> c.getIdMedico() != idMedicoLogueado);
        }

        ObservableList<Cita> pendientesList = FXCollections.observableArrayList(pendientes);

        ComboBox<Cita> cbCitas = new ComboBox<>();
        cbCitas.setPrefWidth(500);
        cbCitas.setItems(pendientesList);
        cbCitas.setPromptText("Seleccione una cita programada...");
        cbCitas.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cita item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item.getIdCita() + " - " + item.getPacienteNombre() +
                           " | " + item.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });
        cbCitas.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cita item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item.getIdCita() + " - " + item.getPacienteNombre());
                }
            }
        });

        TextArea txtSintomas = new TextArea();
        txtSintomas.setPrefHeight(80);
        txtSintomas.setPromptText("Síntomas...");
        TextArea txtDiagnostico = new TextArea();
        txtDiagnostico.setPrefHeight(80);
        txtDiagnostico.setPromptText("Diagnóstico...");
        TextArea txtTratamiento = new TextArea();
        txtTratamiento.setPrefHeight(80);
        txtTratamiento.setPromptText("Tratamiento...");

        final double precioMedico = idMedicoLogueado > 0 ? new MedicoDAO().obtenerPrecioConsulta(idMedicoLogueado) : 0.0;

        TextField txtPrecio = new TextField(String.format("%.0f", precioMedico));
        txtPrecio.setEditable(false);
        txtPrecio.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #115e59; -fx-font-weight: 600;");

        txtSintomas.setDisable(true);
        txtDiagnostico.setDisable(true);
        txtTratamiento.setDisable(true);

        cbCitas.setOnAction(e -> {
            boolean seleccionada = cbCitas.getValue() != null;
            txtSintomas.setDisable(!seleccionada);
            txtDiagnostico.setDisable(!seleccionada);
            txtTratamiento.setDisable(!seleccionada);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Cita programada:"), 0, 0);
        grid.add(cbCitas, 1, 0);
        grid.add(new Label("Síntomas:"), 0, 1);
        grid.add(txtSintomas, 1, 1);
        grid.add(new Label("Diagnóstico:"), 0, 2);
        grid.add(txtDiagnostico, 1, 2);
        grid.add(new Label("Tratamiento:"), 0, 3);
        grid.add(txtTratamiento, 1, 3);
        grid.add(new Label("Precio ($):"), 0, 4);
        grid.add(txtPrecio, 1, 4);

        ButtonType btnGuardar = new ButtonType("Guardar Consulta", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(600);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                Cita seleccionada = cbCitas.getValue();
                if (seleccionada == null) {
                    mostrarError("Seleccione una cita programada.");
                    return null;
                }
                String sintomas = txtSintomas.getText().trim();
                String diagnostico = txtDiagnostico.getText().trim();
                String tratamiento = txtTratamiento.getText().trim();
                if (sintomas.isEmpty() || diagnostico.isEmpty()) {
                    mostrarError("Síntomas y diagnóstico son obligatorios.");
                    return null;
                }
                double precio = precioMedico;
                Consulta consulta = new Consulta(seleccionada.getIdCita(), diagnostico, sintomas, tratamiento, precio);
                consulta.setFechaConsulta(LocalDateTime.now());
                int idConsulta = consultaDAO.insertarConsultaYActualizarEstado(consulta);
                if (idConsulta > 0) {
                    mostrarExito("Consulta registrada correctamente.");
                    dialog.close();
                    cargarHistorial();
                } else {
                    mostrarError("Error al registrar la consulta.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @Override
    public void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    @Override
    public void mostrarExito(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    @Override public int getIdCitaSeleccionada() { return -1; }
    @Override public String getDiagnostico() { return ""; }
    @Override public String getSintomas() { return ""; }
    @Override public String getTratamiento() { return ""; }
    @Override public double getPrecio() { return 0.0; }
    @Override public void mostrarCitasPendientes(List<Cita> citas) {}
    @Override public void limpiarFormulario() {}
    @Override public void limpiarSeleccionCita() {}
    @Override public void actualizarEstadoAcciones(boolean consultaGuardada) {}
}
