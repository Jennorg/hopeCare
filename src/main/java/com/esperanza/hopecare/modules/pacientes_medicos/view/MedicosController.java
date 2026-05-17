package com.esperanza.hopecare.modules.pacientes_medicos.view;

import com.esperanza.hopecare.modules.pacientes_medicos.dao.MedicoDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Medico;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MedicosController {

    @FXML private TextField txtBuscarMedico;
    @FXML private TableView<Medico> tblMedicos;
    @FXML private TableColumn<Medico, Integer> colId;
    @FXML private TableColumn<Medico, String> colNombre;
    @FXML private TableColumn<Medico, String> colDocumento;
    @FXML private TableColumn<Medico, String> colEspecialidad;
    @FXML private TableColumn<Medico, String> colRegistro;
    @FXML private TableColumn<Medico, Double> colPrecio;
    @FXML private TableColumn<Medico, String> colTelefono;
    @FXML private TableColumn<Medico, String> colEmail;
    @FXML private TableColumn<Medico, Boolean> colEstado;
    @FXML private TableColumn<Medico, Void> colAcciones;

    @FXML private Button btnAgregar;

    private MedicoDAO medicoDAO;
    private ObservableList<Medico> medicosList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        medicoDAO = new MedicoDAO();
        configurarColumnas();
        cargarMedicos();
        configurarFiltroReactivo();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMedico"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));
        colEspecialidad.setCellValueFactory(new PropertyValueFactory<>("nombreEspecialidad"));
        colRegistro.setCellValueFactory(new PropertyValueFactory<>("registroMedico"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Format price column as currency
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioConsulta"));
        colPrecio.setCellFactory(column -> new TableCell<Medico, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // Format state column with Teal and Red colors
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colEstado.setCellFactory(column -> new TableCell<Medico, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    boolean activo = getTableRow().getItem().isActivo();
                    setText(activo ? "ACTIVO" : "INACTIVO");
                    if (activo) {
                        setStyle("-fx-text-fill: #0d9488; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 600;");
                    }
                }
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<Medico, Void>() {
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
                btnEdit.setTooltip(new Tooltip("Editar Médico"));

                btnDelete.getStyleClass().add("button-action-delete");
                javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                deleteIcon.setContent("M2 4h12M4 4v10a1 1 0 001 1h6a1 1 0 001-1V4M6 4V2h4v2");
                deleteIcon.getStyleClass().add("svg-icon");
                btnDelete.setGraphic(deleteIcon);
                btnDelete.setTooltip(new Tooltip("Dar de Baja"));

                btnEdit.setOnAction(e -> {
                    Medico m = getTableView().getItems().get(getIndex());
                    if (m != null) {
                        mostrarFormulario(true, m);
                    }
                });

                btnDelete.setOnAction(e -> {
                    Medico m = getTableView().getItems().get(getIndex());
                    if (m != null) {
                        eliminarMedico(m);
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

    private void cargarMedicos() {
        medicosList.clear();
        medicosList.addAll(medicoDAO.listarTodos());
    }

    private void configurarFiltroReactivo() {
        FilteredList<Medico> filteredList = new FilteredList<>(medicosList, m -> true);
        txtBuscarMedico.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(m -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                if (m.getNombreCompleto().toLowerCase().contains(filter)) {
                    return true;
                }
                if (m.getDocumentoIdentidad() != null && m.getDocumentoIdentidad().contains(filter)) {
                    return true;
                }
                if (m.getNombreEspecialidad() != null && m.getNombreEspecialidad().toLowerCase().contains(filter)) {
                    return true;
                }
                if (m.getRegistroMedico() != null && m.getRegistroMedico().toLowerCase().contains(filter)) {
                    return true;
                }
                return false;
            });
        });
        tblMedicos.setItems(filteredList);
    }

    @FXML
    private void agregarMedicoClick() {
        mostrarFormulario(false, null);
    }

    private void eliminarMedico(Medico selected) {
        if (!selected.isActivo()) {
            mostrarAlerta("Información", "El médico seleccionado ya se encuentra inactivo.", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Baja");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de dar de baja (desactivar) al médico " + selected.getNombreCompleto() + "?\nNo se eliminará físicamente para conservar el historial de citas.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = medicoDAO.eliminarMedicoLogico(selected.getIdMedico());
                if (ok) {
                    mostrarAlerta("Éxito", "Médico dado de baja correctamente.", Alert.AlertType.INFORMATION);
                    cargarMedicos();
                } else {
                    mostrarAlerta("Error", "No se pudo desactivar el médico de la base de datos.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void mostrarFormulario(boolean isEdit, Medico selectedMedico) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(isEdit ? "Editar Médico" : "Registrar Médico");
            dialog.setHeaderText(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/modules/pacientes_medicos/view/medico_form.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            MedicoFormController formController = loader.getController();
            if (isEdit) {
                formController.cargarMedico(selectedMedico);
            } else {
                formController.cargarMedico(new Medico());
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
                    mostrarAlerta("Validación", "Los campos marcados con * son obligatorios y el precio debe ser numérico.", Alert.AlertType.ERROR);
                    event.consume();
                    return;
                }

                Medico m = formController.obtenerMedicoModificado();
                if (medicoDAO.existeDocumento(m.getDocumentoIdentidad(), m.getIdPersona())) {
                    mostrarAlerta("Validación", "Ya existe una persona registrada con este documento de identidad.", Alert.AlertType.ERROR);
                    event.consume();
                    return;
                }

                if (medicoDAO.existeRegistroMedico(m.getRegistroMedico(), m.getIdMedico())) {
                    mostrarAlerta("Validación", "Ya existe un médico registrado con este número de registro médico.", Alert.AlertType.ERROR);
                    event.consume();
                }
            });

            dialog.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    Medico m = formController.obtenerMedicoModificado();
                    boolean saved;
                    if (isEdit) {
                        saved = medicoDAO.actualizarMedico(m);
                    } else {
                        saved = medicoDAO.insertarMedico(m);
                    }

                    if (saved) {
                        mostrarAlerta("Éxito", isEdit ? "Médico actualizado correctamente." : "Médico registrado correctamente.", Alert.AlertType.INFORMATION);
                        cargarMedicos();
                    } else {
                        mostrarAlerta("Error", "No se pudo guardar los datos del médico.", Alert.AlertType.ERROR);
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
