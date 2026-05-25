package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.util.SesionManager;
import com.esperanza.hopecare.dao.PacienteDAO;
import com.esperanza.hopecare.model.Paciente;
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
    @FXML private TableColumn<Paciente, String> colEstado;
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
        aplicarPermisos();
    }

    private void aplicarPermisos() {
        SesionManager sesion = SesionManager.getInstance();
        if (sesion.isMedico()) {
            btnAgregar.setVisible(false);
            btnAgregar.setManaged(false);
            // colAcciones permanece visible para que puedan dar de alta
        }
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

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> new TableCell<Paciente, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Activo".equals(item)) {
                        setStyle("-fx-text-fill: #0d9488; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-alignment: center;");
                    }
                }
            }
        });

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

                btnEdit.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        mostrarFormulario(true, p);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Paciente p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        SesionManager sesion = SesionManager.getInstance();
                        boolean esMedico = sesion.isMedico();
                        
                        btnEdit.setVisible(!esMedico);
                        btnEdit.setManaged(!esMedico);
                        
                        btnDelete.getStyleClass().clear();
                        btnDelete.getStyleClass().add("button");
                        
                        javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                        deleteIcon.getStyleClass().add("svg-icon");
                        
                        if (p.getActivo() == 1) {
                            btnDelete.getStyleClass().add("button-action-discharge");
                            deleteIcon.setContent("M16 11h6M13 9a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0");
                            btnDelete.setGraphic(deleteIcon);
                            btnDelete.setTooltip(new Tooltip("Dar de alta a Paciente"));
                            btnDelete.setOnAction(e -> darAltaPaciente(p));
                            btnDelete.setVisible(true);
                            btnDelete.setManaged(true);
                        } else {
                            btnDelete.getStyleClass().add("button-action-reactivate");
                            deleteIcon.setContent("M16 11h6m-3-3v6M13 9a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0");
                            btnDelete.setGraphic(deleteIcon);
                            btnDelete.setTooltip(new Tooltip("Reactivar Paciente"));
                            btnDelete.setOnAction(e -> reactivarPaciente(p));
                            btnDelete.setVisible(true);
                            btnDelete.setManaged(true);
                        }
                    }
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

    private void darAltaPaciente(Paciente selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Alta Médica");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de dar de alta (inactivar) al paciente " + selected.getNombreCompleto() + "?");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = pacienteDAO.darDeAlta(selected.getIdPaciente());
                if (ok) {
                    mostrarAlerta("Éxito", "Paciente dado de alta correctamente.", Alert.AlertType.INFORMATION);
                    cargarPacientes();
                } else {
                    mostrarAlerta("Error", "No se pudo dar de alta al paciente.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void reactivarPaciente(Paciente selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Reactivación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de reactivar al paciente " + selected.getNombreCompleto() + "?");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = pacienteDAO.reactivar(selected.getIdPaciente());
                if (ok) {
                    mostrarAlerta("Éxito", "Paciente reactivado correctamente.", Alert.AlertType.INFORMATION);
                    cargarPacientes();
                } else {
                    mostrarAlerta("Error", "No se pudo reactivar al paciente.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void mostrarFormulario(boolean isEdit, Paciente selectedPaciente) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(isEdit ? "Editar Paciente" : "Registrar Paciente");
            dialog.setHeaderText(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esperanza/hopecare/view/paciente_form.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            if (tblPacientes.getScene() != null && tblPacientes.getScene().getWindow() != null) {
                javafx.stage.Window owner = tblPacientes.getScene().getWindow();
                dialog.initOwner(owner);
                double targetHeight = owner.getHeight() * 0.9;
                dialogPane.setPrefHeight(targetHeight);
            }

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
                    mostrarAlerta("Validación", formController.getMensajeError(), Alert.AlertType.ERROR);
                    event.consume();
                    return;
                }

                Paciente p = formController.obtenerPacienteModificado();
                if (pacienteDAO.existeDocumento(p.getDocumentoIdentidad(), p.getIdPersona())) {
                    mostrarAlerta("Validación", "Ya existe una persona registrada con esta cédula.", Alert.AlertType.ERROR);
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
