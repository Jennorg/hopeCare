package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.util.SesionManager;
import com.esperanza.hopecare.dao.MedicoDAO;
import com.esperanza.hopecare.model.Medico;
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
        aplicarPermisos();
    }

    /**
     * Aplica restricciones visuales basadas en el rol del usuario autenticado.
     * - MEDICOS: Acceso de solo lectura (sin creación ni edición).
     * - RECEPCIONISTAS: Pueden editar pero no registrar nuevos médicos.
     * - ADMINS: Tienen acceso total.
     */
    private void aplicarPermisos() {
        SesionManager sesion = SesionManager.getInstance();
        if (sesion.isMedico() || sesion.isRecepcionista()) {
            btnAgregar.setVisible(false);
            btnAgregar.setManaged(false);
            colAcciones.setVisible(false);
        }
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
            private final Button btnToggleStatus = new Button();
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(8, btnEdit, btnToggleStatus);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnEdit.getStyleClass().add("button-action-edit");
                javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
                editIcon.setContent("M11 2h3v3L5 14l-3 1 1-3L11 2z");
                editIcon.getStyleClass().add("svg-icon");
                btnEdit.setGraphic(editIcon);
                btnEdit.setTooltip(new Tooltip("Editar Médico"));

                btnEdit.setOnAction(e -> {
                    Medico m = getTableView().getItems().get(getIndex());
                    if (m != null) {
                        mostrarFormulario(true, m);
                    }
                });

                btnToggleStatus.setOnAction(e -> {
                    Medico m = getTableView().getItems().get(getIndex());
                    if (m != null) {
                        gestionarEstadoMedico(m);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || SesionManager.getInstance().isMedico()) {
                    setGraphic(null);
                } else {
                    Medico m = getTableRow().getItem();
                    if (m != null) {
                        boolean activo = m.isActivo();
                        
                        btnToggleStatus.getStyleClass().removeAll("button-action-delete", "button-action-reactivate");
                        btnToggleStatus.getStyleClass().add(activo ? "button-action-delete" : "button-action-reactivate");
                        
                        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
                        if (activo) {
                            icon.setContent("M2 4h12M4 4v10a1 1 0 001 1h6a1 1 0 001-1V4M6 4V2h4v2");
                            btnToggleStatus.setTooltip(new Tooltip("Dar de Baja"));
                        } else {
                            icon.setContent("M12 2v4M12 2l-3 3M12 2l3 3M4 12c0 4.4 3.6 8 8 8s8-3.6 8-8-3.6-8-8-8"); // Refrescar/Reactivar
                            btnToggleStatus.setTooltip(new Tooltip("Reactivar Médico"));
                        }
                        icon.getStyleClass().add("svg-icon");
                        btnToggleStatus.setGraphic(icon);
                    }
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

    private void gestionarEstadoMedico(Medico selected) {
        boolean actualmenteActivo = selected.isActivo();
        String accion = actualmenteActivo ? "desactivar" : "reactivar";
        String titulo = actualmenteActivo ? "Confirmar Baja" : "Confirmar Reactivación";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(titulo);
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de " + accion + " al médico " + selected.getNombreCompleto() + "?");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = medicoDAO.cambiarEstadoActivo(selected.getIdMedico(), actualmenteActivo ? 0 : 1);
                if (ok) {
                    String msgExito = actualmenteActivo ? "Médico desactivado correctamente." : "Médico reactivado correctamente.";
                    mostrarAlerta("Éxito", msgExito, Alert.AlertType.INFORMATION);
                    cargarMedicos();
                } else {
                    mostrarAlerta("Error", "No se pudo cambiar el estado del médico en la base de datos.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void mostrarFormulario(boolean isEdit, Medico selectedMedico) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(isEdit ? "Editar Médico" : "Registrar Médico");
            dialog.setHeaderText(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/view/medico_form.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            if (tblMedicos.getScene() != null && tblMedicos.getScene().getWindow() != null) {
                javafx.stage.Window owner = tblMedicos.getScene().getWindow();
                dialog.initOwner(owner);
                double targetHeight = owner.getHeight() * 0.9;
                dialogPane.setPrefHeight(targetHeight);
            }

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
                    mostrarAlerta("Validación", formController.getMensajeError(), Alert.AlertType.ERROR);
                    event.consume();
                    return;
                }

                Medico m = formController.obtenerMedicoModificado();
                if (medicoDAO.existeDocumento(m.getDocumentoIdentidad(), m.getIdPersona())) {
                    mostrarAlerta("Validación", "Ya existe una persona registrada con esta cédula.", Alert.AlertType.ERROR);
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
