package com.esperanza.hopecare.modules.pacientes_medicos.view;

import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class PacientesController {

    @FXML private TextField txtBuscarPaciente;
    @FXML private TableView<Paciente> tblPacientes;
    @FXML private TableColumn<Paciente, Integer> colId;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, String> colDocumento;
    @FXML private TableColumn<Paciente, String> colFechaNac;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, String> colEmail;
    @FXML private TableColumn<Paciente, String> colHistoria;
    @FXML private TableColumn<Paciente, String> colGrupoSangre;
    @FXML private TableColumn<Paciente, String> colAlergias;
    @FXML private TableColumn<Paciente, String> colContacto;
    @FXML private TableColumn<Paciente, Void> colAcciones;

    @FXML private Button btnAgregar;

    private PacienteDAO pacienteDAO;
    private ObservableList<Paciente> pacientesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        pacienteDAO = new PacienteDAO();
        configurarColumnas();
        cargarPacientes();
        configurarFiltroReactivo();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPaciente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));
        colFechaNac.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colHistoria.setCellValueFactory(new PropertyValueFactory<>("historiaClinica"));
        colGrupoSangre.setCellValueFactory(new PropertyValueFactory<>("grupoSanguineo"));
        colAlergias.setCellValueFactory(new PropertyValueFactory<>("alergias"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contactoEmergencia"));

        colAcciones.setCellFactory(col -> new TableCell<Paciente, Void>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(8, btnEdit, btnDelete);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnEdit.getStyleClass().add("button-action-edit");
                javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
                editIcon.setContent("M11 2h3v3L5 14l-3 1 1-3L11 2z");
                editIcon.getStyleClass().add("svg-icon");
                btnEdit.setGraphic(editIcon);
                btnEdit.setTooltip(new Tooltip("Editar Paciente"));

                btnDelete.getStyleClass().add("button-action-delete");
                javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                deleteIcon.setContent("M2 4h12M4 4v10a1 1 0 001 1h6a1 1 0 001-1V4M6 4V2h4v2");
                deleteIcon.getStyleClass().add("svg-icon");
                btnDelete.setGraphic(deleteIcon);
                btnDelete.setTooltip(new Tooltip("Eliminar Paciente"));

                btnEdit.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        mostrarFormulario(true, p);
                    }
                });

                btnDelete.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        eliminarPaciente(p);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void cargarPacientes() {
        pacientesList.clear();
        pacientesList.addAll(pacienteDAO.listarTodos());
    }

    private void configurarFiltroReactivo() {
        FilteredList<Paciente> filteredList = new FilteredList<>(pacientesList, p -> true);
        txtBuscarPaciente.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(p -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                if (p.getNombreCompleto().toLowerCase().contains(filter)) {
                    return true;
                }
                if (p.getDocumentoIdentidad() != null && p.getDocumentoIdentidad().contains(filter)) {
                    return true;
                }
                if (p.getHistoriaClinica() != null && p.getHistoriaClinica().toLowerCase().contains(filter)) {
                    return true;
                }
                return false;
            });
        });
        tblPacientes.setItems(filteredList);
    }

    @FXML
    private void agregarPacienteClick() {
        mostrarFormulario(false, null);
    }

    private void eliminarPaciente(Paciente selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de eliminar de forma permanente al paciente " + selected.getNombreCompleto() + "?\nEsta acción no se puede deshacer.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    boolean ok = pacienteDAO.eliminar(selected.getIdPaciente());
                    if (ok) {
                        mostrarAlerta("Éxito", "Paciente eliminado correctamente.", Alert.AlertType.INFORMATION);
                        cargarPacientes();
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el paciente de la base de datos.", Alert.AlertType.ERROR);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error de Integridad", "El paciente posee citas, consultas, facturas o registros clínicos activos y no puede ser eliminado.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void mostrarFormulario(boolean isEdit, Paciente selectedPaciente) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(isEdit ? "Editar Paciente" : "Registrar Paciente");
            dialog.setHeaderText(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/modules/pacientes_medicos/view/paciente_form.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            PacienteFormController formController = loader.getController();
            if (isEdit) {
                formController.cargarPaciente(selectedPaciente);
            } else {
                formController.cargarPaciente(new Paciente());
            }

            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.getStyleClass().add("button");
            okButton.setText("Guardar");

            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            cancelButton.getStyleClass().add("button-secondary");
            cancelButton.setText("Cancelar");

            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!formController.validar()) {
                    mostrarAlerta("Validación", "Los campos marcados con * son obligatorios.", Alert.AlertType.ERROR);
                    event.consume();
                    return;
                }

                Paciente p = formController.obtenerPacienteModificado();
                if (pacienteDAO.existeDocumento(p.getDocumentoIdentidad(), p.getIdPersona())) {
                    mostrarAlerta("Validación", "Ya existe una persona registrada con este documento de identidad.", Alert.AlertType.ERROR);
                    event.consume();
                    return;
                }

                if (pacienteDAO.existeHistoriaClinica(p.getHistoriaClinica(), p.getIdPaciente())) {
                    mostrarAlerta("Validación", "Ya existe un paciente con este número de historia clínica.", Alert.AlertType.ERROR);
                    event.consume();
                }
            });

            dialog.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    Paciente p = formController.obtenerPacienteModificado();
                    boolean saved;
                    if (isEdit) {
                        saved = pacienteDAO.actualizar(p);
                    } else {
                        saved = pacienteDAO.insertar(p);
                    }

                    if (saved) {
                        mostrarAlerta("Éxito", isEdit ? "Paciente actualizado correctamente." : "Paciente registrado correctamente.", Alert.AlertType.INFORMATION);
                        cargarPacientes();
                    } else {
                        mostrarAlerta("Error", "No se pudo guardar los datos del paciente.", Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Interfaz", "Error al cargar la interfaz del formulario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
