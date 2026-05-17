package com.esperanza.hopecare.modules.medicamentos_lab.ui;

import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.MedicamentoDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.EntregaMedicamentoDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.model.Medicamento;
import com.esperanza.hopecare.modules.medicamentos_lab.model.EntregaMedicamento;
import com.esperanza.hopecare.modules.medicamentos_lab.service.InventarioService;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import com.esperanza.hopecare.common.db.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class FarmaciaController {
    @FXML private TextField txtBuscarMedicamento;
    @FXML private Button btnAgregarMedicamento;
    @FXML private TableView<Medicamento> tblInventario;
    @FXML private TableColumn<Medicamento, Integer> colMedicamentoId;
    @FXML private TableColumn<Medicamento, String> colMedicamentoNombre;
    @FXML private TableColumn<Medicamento, Integer> colStockActual;
    @FXML private TableColumn<Medicamento, Integer> colStockMinimo;
    @FXML private TableColumn<Medicamento, String> colEstado;
    @FXML private TableColumn<Medicamento, Void> colAccionesInventario;

    @FXML private TableView<EntregaMedicamento> tblEntregas;
    @FXML private TableColumn<EntregaMedicamento, Integer> colEntregaId;
    @FXML private TableColumn<EntregaMedicamento, String> colEntregaPaciente;
    @FXML private TableColumn<EntregaMedicamento, String> colEntregaMedicamento;
    @FXML private TableColumn<EntregaMedicamento, Integer> colEntregaCantidad;
    @FXML private TableColumn<EntregaMedicamento, String> colEntregaReceta;
    @FXML private TableColumn<EntregaMedicamento, String> colEntregaFecha;
    @FXML private TableColumn<EntregaMedicamento, String> colEntregaFacturado;
    @FXML private TableColumn<EntregaMedicamento, Void> colAccionesEntregas;

    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<Medicamento> cbMedicamento;
    @FXML private TextField txtCantidad;
    @FXML private CheckBox chkPresentoReceta;
    @FXML private Button btnEntregar;

    private GestionClinicaFacade facade;
    private MedicamentoDAO medicamentoDAO;
    private EntregaMedicamentoDAO entregaDAO;
    private PacienteDAO pacienteDAO;
    private InventarioService inventarioService;

    private ObservableList<Medicamento> inventarioData = FXCollections.observableArrayList();
    private ObservableList<EntregaMedicamento> entregasData = FXCollections.observableArrayList();
    private ObservableList<Paciente> pacientesData = FXCollections.observableArrayList();
    private ObservableList<Medicamento> medicamentosData = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        facade = new GestionClinicaFacade();
        medicamentoDAO = new MedicamentoDAO();
        entregaDAO = new EntregaMedicamentoDAO();
        pacienteDAO = new PacienteDAO();
        inventarioService = new InventarioService();

        configurarColumnasInventario();
        configurarColumnasEntregas();
        configurarComboBoxes();

        tblInventario.setItems(inventarioData);
        tblEntregas.setItems(entregasData);

        txtBuscarMedicamento.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrarInventario(newVal);
        });

        cargarDatos();
    }

    private void configurarColumnasInventario() {
        colMedicamentoId.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colMedicamentoNombre.setCellValueFactory(new PropertyValueFactory<>("nombreComercial"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        colEstado.setCellFactory(column -> new TableCell<Medicamento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    Medicamento med = getTableRow().getItem();
                    if (med.getStockActual() <= med.getStockMinimo()) {
                        setText("BAJO");
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 600;");
                    } else {
                        setText("OK");
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 600;");
                    }
                }
            }
        });

        colAccionesInventario.setCellFactory(col -> new TableCell<Medicamento, Void>() {
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
                btnEdit.setTooltip(new Tooltip("Actualizar Stock"));

                btnDelete.getStyleClass().add("button-action-delete");
                javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                deleteIcon.setContent("M2 4h12M4 4v10a1 1 0 001 1h6a1 1 0 001-1V4M6 4V2h4v2");
                deleteIcon.getStyleClass().add("svg-icon");
                btnDelete.setGraphic(deleteIcon);
                btnDelete.setTooltip(new Tooltip("Eliminar Medicamento"));

                btnEdit.setOnAction(e -> {
                    Medicamento m = getTableView().getItems().get(getIndex());
                    if (m != null) {
                        actualizarStock(m);
                    }
                });

                btnDelete.setOnAction(e -> {
                    Medicamento m = getTableView().getItems().get(getIndex());
                    if (m != null) {
                        eliminarMedicamento(m);
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

    private void configurarColumnasEntregas() {
        colEntregaId.setCellValueFactory(new PropertyValueFactory<>("idEntrega"));
        colEntregaPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNombreCompleto"));
        colEntregaMedicamento.setCellValueFactory(new PropertyValueFactory<>("medicamentoNombre"));
        colEntregaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadEntregada"));
        colEntregaReceta.setCellFactory(column -> new TableCell<EntregaMedicamento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    EntregaMedicamento ent = getTableRow().getItem();
                    if (ent.isPresenteReceta()) {
                        setText("SÍ");
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 600;");
                    } else {
                        setText("NO");
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 600;");
                    }
                }
            }
        });
        colEntregaFecha.setCellFactory(column -> new TableCell<EntregaMedicamento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(getTableRow().getItem().getFechaEntrega() != null ?
                        getTableRow().getItem().getFechaEntrega().format(dateFormatter) : "");
                }
            }
        });
        colEntregaFacturado.setCellFactory(column -> new TableCell<EntregaMedicamento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(getTableRow().getItem().isFacturado() ? "SÍ" : "NO");
                    setStyle(getTableRow().getItem().isFacturado() ?
                        "-fx-text-fill: #16a34a;" : "-fx-text-fill: #f59e0b;");
                }
            }
        });

        colAccionesEntregas.setCellFactory(col -> new TableCell<EntregaMedicamento, Void>() {
            private final Button btnCancel = new Button();
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(btnCancel);

            {
                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnCancel.getStyleClass().add("button-action-delete");
                javafx.scene.shape.SVGPath cancelIcon = new javafx.scene.shape.SVGPath();
                cancelIcon.setContent("M2 4h12M4 4v10a1 1 0 001 1h6a1 1 0 001-1V4M6 4V2h4v2");
                cancelIcon.getStyleClass().add("svg-icon");
                btnCancel.setGraphic(cancelIcon);
                btnCancel.setTooltip(new Tooltip("Cancelar Entrega"));

                btnCancel.setOnAction(e -> {
                    EntregaMedicamento ent = getTableView().getItems().get(getIndex());
                    if (ent != null) {
                        cancelarEntrega(ent);
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

    private void configurarComboBoxes() {
        cbPaciente.setItems(pacientesData);
        cbPaciente.setCellFactory(lv -> new ListCell<Paciente>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreCompleto() : null);
            }
        });
        cbPaciente.setButtonCell(new ListCell<Paciente>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreCompleto() : "Seleccionar paciente...");
            }
        });

        cbMedicamento.setItems(medicamentosData);
        cbMedicamento.setCellFactory(lv -> new ListCell<Medicamento>() {
            @Override
            protected void updateItem(Medicamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreComercial() : null);
            }
        });
        cbMedicamento.setButtonCell(new ListCell<Medicamento>() {
            @Override
            protected void updateItem(Medicamento item, boolean empty) {
                super.updateItem(item, empty);
                setText(item != null ? item.getNombreComercial() : "Seleccionar medicamento...");
            }
        });
    }

    private void cargarDatos() {
        cargarInventario();
        cargarPacientes();
        cargarMedicamentos();
        cargarEntregas();
    }

    private void cargarInventario() {
        inventarioData.clear();
        inventarioData.addAll(medicamentoDAO.listarTodos());
    }

    private void cargarPacientes() {
        pacientesData.clear();
        pacientesData.addAll(pacienteDAO.listarTodos());
    }

    private void cargarMedicamentos() {
        medicamentosData.clear();
        medicamentosData.addAll(medicamentoDAO.listarTodos());
    }

    private void cargarEntregas() {
        entregasData.clear();
        entregasData.addAll(entregaDAO.listarTodas());
    }

    private void filtrarInventario(String filtro) {
        inventarioData.clear();
        if (filtro == null || filtro.trim().isEmpty()) {
            inventarioData.addAll(medicamentoDAO.listarTodos());
        } else {
            String lowerFiltro = filtro.toLowerCase();
            medicamentoDAO.listarTodos().stream()
                .filter(m -> m.getNombreComercial().toLowerCase().contains(lowerFiltro))
                .forEach(inventarioData::add);
        }
    }

    @FXML
    private void btnEntregarClick() {
        Paciente paciente = cbPaciente.getValue();
        Medicamento medicamento = cbMedicamento.getValue();
        String strCantidad = txtCantidad.getText().trim();

        if (paciente == null || medicamento == null || strCantidad.isEmpty()) {
            mostrarAlerta("Error", "Complete todos los campos para la entrega", Alert.AlertType.ERROR);
            return;
        }

        try {
            int cantidad = Integer.parseInt(strCantidad);
            if (cantidad <= 0) {
                mostrarAlerta("Error", "La cantidad debe ser mayor a 0", Alert.AlertType.ERROR);
                return;
            }

            boolean presenteReceta = chkPresentoReceta.isSelected();
            boolean ok = facade.procesarEntregaMedicamento(paciente.getIdPaciente(), medicamento.getIdMedicamento(), cantidad, presenteReceta, "FARMACIA");
            if (ok) {
                mostrarAlerta("Éxito", "Entrega registrada y stock actualizado", Alert.AlertType.INFORMATION);
                cargarDatos();
                limpiarCamposEntrega();
            } else {
                mostrarAlerta("Error", "Stock insuficiente o datos inválidos", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad debe ser numérica", Alert.AlertType.ERROR);
        }
    }

    private void limpiarCamposEntrega() {
        cbPaciente.getSelectionModel().clearSelection();
        cbMedicamento.getSelectionModel().clearSelection();
        txtCantidad.clear();
        chkPresentoReceta.setSelected(false);
    }

    @FXML
    private void agregarMedicamentoClick() {
        Dialog<Medicamento> dialog = new Dialog<>();
        dialog.setTitle("Agregar Medicamento");
        dialog.setHeaderText("Ingrese los datos del nuevo medicamento");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del medicamento");
        TextField txtPrincipio = new TextField();
        txtPrincipio.setPromptText("Principio activo");
        TextField txtPresentacion = new TextField();
        txtPresentacion.setPromptText("Presentación");
        TextField txtConcentracion = new TextField();
        txtConcentracion.setPromptText("Concentración");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio unitario");
        TextField txtStock = new TextField();
        txtStock.setText("0");
        TextField txtStockMin = new TextField();
        txtStockMin.setText("0");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Principio:"), 0, 1); grid.add(txtPrincipio, 1, 1);
        grid.add(new Label("Presentación:"), 0, 2); grid.add(txtPresentacion, 1, 2);
        grid.add(new Label("Concentración:"), 0, 3); grid.add(txtConcentracion, 1, 3);
        grid.add(new Label("Precio:"), 0, 4); grid.add(txtPrecio, 1, 4);
        grid.add(new Label("Stock:"), 0, 5); grid.add(txtStock, 1, 5);
        grid.add(new Label("Stock Mín:"), 0, 6); grid.add(txtStockMin, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String nombre = txtNombre.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarAlerta("Error", "El nombre es obligatorio", Alert.AlertType.ERROR);
                    return null;
                }
                try {
                    double precio = Double.parseDouble(txtPrecio.getText().trim());
                    int stock = Integer.parseInt(txtStock.getText().trim());
                    int stockMin = Integer.parseInt(txtStockMin.getText().trim());
                    return new Medicamento(0, nombre, txtPrincipio.getText().trim(), txtPresentacion.getText().trim(), txtConcentracion.getText().trim(), precio, stock, stockMin, true);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "Valores numéricos inválidos", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(medicamento -> {
            if (inventarioService.agregarMedicamento(medicamento)) {
                mostrarAlerta("Éxito", "Medicamento agregado correctamente", Alert.AlertType.INFORMATION);
                cargarInventario();
                cargarMedicamentos();
            } else {
                mostrarAlerta("Error", "No se pudo agregar el medicamento", Alert.AlertType.ERROR);
            }
        });
    }

    private void actualizarStock(Medicamento selected) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Actualizar Stock");
        dialog.setHeaderText("Nuevo stock para: " + selected.getNombreComercial());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        TextField txtNuevoStock = new TextField();
        txtNuevoStock.setText(String.valueOf(selected.getStockActual()));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.add(new Label("Stock Actual:"), 0, 0);
        grid.add(new Label(String.valueOf(selected.getStockActual())), 1, 0);
        grid.add(new Label("Nuevo Stock:"), 0, 1);
        grid.add(txtNuevoStock, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    return Integer.parseInt(txtNuevoStock.getText().trim());
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "El valor debe ser numérico", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevoStock -> {
            if (inventarioService.actualizarStock(selected.getIdMedicamento(), nuevoStock)) {
                mostrarAlerta("Éxito", "Stock actualizado correctamente", Alert.AlertType.INFORMATION);
                cargarInventario();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el stock", Alert.AlertType.ERROR);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void eliminarMedicamento(Medicamento selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de eliminar el medicamento '" + selected.getNombreComercial() + "'?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    boolean ok = medicamentoDAO.eliminar(selected.getIdMedicamento(), conn);
                    if (ok) {
                        conn.commit();
                        mostrarAlerta("Éxito", "Medicamento eliminado correctamente", Alert.AlertType.INFORMATION);
                        cargarInventario();
                        cargarMedicamentos();
                    } else {
                        conn.rollback();
                        mostrarAlerta("Error", "No se pudo eliminar el medicamento", Alert.AlertType.ERROR);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error de base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cancelarEntrega(EntregaMedicamento selected) {
        if (selected.isFacturado()) {
            mostrarAlerta("Error", "No se puede cancelar una entrega que ya ha sido facturada", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cancelación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de cancelar esta entrega? El stock del medicamento será revertido.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    boolean ok = entregaDAO.eliminar(selected.getIdEntrega(), conn);
                    if (ok) {
                        conn.commit();
                        mostrarAlerta("Éxito", "Entrega cancelada y stock revertido", Alert.AlertType.INFORMATION);
                        cargarDatos();
                    } else {
                        conn.rollback();
                        mostrarAlerta("Error", "No se pudo cancelar la entrega", Alert.AlertType.ERROR);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error de base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
}
