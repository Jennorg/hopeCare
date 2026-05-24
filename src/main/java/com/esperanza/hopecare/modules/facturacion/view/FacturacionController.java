package com.esperanza.hopecare.modules.facturacion.view;

import com.esperanza.hopecare.common.events.DatosFacturablesActualizadosEvent;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.session.SesionManager;
import com.esperanza.hopecare.modules.facturacion.dao.FacturaConsultaDAO;
import com.esperanza.hopecare.modules.facturacion.dao.FacturaDAO;
import com.esperanza.hopecare.modules.facturacion.dto.FacturaDTO;
import com.esperanza.hopecare.modules.facturacion.dto.FacturaResumenDTO;
import com.esperanza.hopecare.modules.facturacion.dto.PendienteDTO;
import com.esperanza.hopecare.modules.facturacion.service.FacturacionService;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.stream.Collectors;

public class FacturacionController {
    @FXML private TableView<FacturaResumenDTO> tablaFacturas;
    @FXML private TableColumn<FacturaResumenDTO, Integer> colId;
    @FXML private TableColumn<FacturaResumenDTO, String> colPaciente;
    @FXML private TableColumn<FacturaResumenDTO, String> colFecha;
    @FXML private TableColumn<FacturaResumenDTO, Double> colSubtotal;
    @FXML private TableColumn<FacturaResumenDTO, Double> colImpuesto;
    @FXML private TableColumn<FacturaResumenDTO, Double> colTotal;
    @FXML private TableColumn<FacturaResumenDTO, String> colEstado;

    @FXML private VBox panelPacientes;
    @FXML private TableView<Paciente> tablaPacientes;
    @FXML private TableColumn<Paciente, Integer> colPacId;
    @FXML private TableColumn<Paciente, String> colPacNombre;
    @FXML private TableColumn<Paciente, String> colPacDoc;
    @FXML private TextField txtBuscarPaciente;

    @FXML private TextField txtBuscarCita;
    @FXML private Button btnGenCita;
    @FXML private TableView<PendienteDTO> tablaCitasPendientes;
    @FXML private TableColumn<PendienteDTO, Integer> colCitaIdRef;
    @FXML private TableColumn<PendienteDTO, String> colCitaPaciente;
    @FXML private TableColumn<PendienteDTO, String> colCitaConcepto;
    @FXML private TableColumn<PendienteDTO, Double> colCitaMonto;
    @FXML private TableColumn<PendienteDTO, String> colCitaFecha;

    @FXML private TextField txtBuscarTodos;
    @FXML private Button btnGenTodos;
    @FXML private TableView<PendienteDTO> tablaTodosPendientes;
    @FXML private TableColumn<PendienteDTO, Integer> colTodosIdRef;
    @FXML private TableColumn<PendienteDTO, String> colTodosPaciente;
    @FXML private TableColumn<PendienteDTO, String> colTodosConcepto;
    @FXML private TableColumn<PendienteDTO, Double> colTodosMonto;
    @FXML private TableColumn<PendienteDTO, String> colTodosFecha;
@FXML private TableColumn<PendienteDTO, String> colTodosTipo;
@FXML private Button btnAyuda;

private FacturacionService service;
    private FacturaDAO facturaDAO;
    private ObservableList<FacturaResumenDTO> facturasList;

    private ObservableList<PendienteDTO> citasPendientes;
    private ObservableList<PendienteDTO> todosPendientes;
    private FilteredList<PendienteDTO> citasFiltradas;
    private FilteredList<PendienteDTO> todosFiltrados;

    private FacturaConsultaDAO consultaDAO;

    private PacienteDAO pacienteDAO;
    private ObservableList<Paciente> pacientesList;
    private FilteredList<Paciente> pacientesFiltrados;
    private int selectedPatientId = -1;
    private String selectedPatientName = "";

    private String currentRole;
    private int currentIdPersona;

    @FXML
    public void initialize() {
        SesionManager sesion = SesionManager.getInstance();
        currentRole = sesion.getNombreRol();
        currentIdPersona = sesion.getIdPersona();

        service = new FacturacionService();
        facturaDAO = new FacturaDAO();
        consultaDAO = new FacturaConsultaDAO();
        pacienteDAO = new PacienteDAO();

        configurarTablaPendientes(colCitaIdRef, colCitaPaciente, colCitaConcepto, colCitaMonto, colCitaFecha);
        configurarTablaTodos(colTodosIdRef, colTodosPaciente, colTodosConcepto, colTodosMonto, colTodosFecha, colTodosTipo);

        citasPendientes = FXCollections.observableArrayList();
        todosPendientes = FXCollections.observableArrayList();

        citasFiltradas = new FilteredList<>(citasPendientes, p -> false);
        todosFiltrados = new FilteredList<>(todosPendientes, p -> false);

        tablaCitasPendientes.setItems(citasFiltradas);
        tablaTodosPendientes.setItems(todosFiltrados);

        txtBuscarCita.textProperty().addListener((obs, o, n) -> aplicarFiltroPendientes());
        txtBuscarTodos.textProperty().addListener((obs, o, n) -> aplicarFiltroPendientes());

        btnGenCita.setOnAction(e -> generarFactura("CONSULTA"));
        btnGenTodos.setOnAction(e -> generarFactura(null));
        btnAyuda.setOnAction(e -> mostrarAyuda());

        if ("PACIENTE".equals(currentRole)) {
            panelPacientes.setVisible(false);
            panelPacientes.setManaged(false);
            btnGenCita.setVisible(false);
            btnGenCita.setManaged(false);
            btnGenTodos.setVisible(false);
            btnGenTodos.setManaged(false);
        } else {
            configurarTablaPacientes();
        }

        configurarTablaFacturas();
        try {
            cargarFacturas();
            cargarPendientes();
            if (!"PACIENTE".equals(currentRole)) {
                cargarPacientes();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        EventBus.getInstance().register(DatosFacturablesActualizadosEvent.class, e -> refrescar());
    }

    private void configurarTablaPacientes() {
        colPacId.setCellValueFactory(new PropertyValueFactory<>("idPaciente"));
        colPacNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colPacDoc.setCellValueFactory(new PropertyValueFactory<>("documentoIdentidad"));

        pacientesList = FXCollections.observableArrayList();
        pacientesFiltrados = new FilteredList<>(pacientesList, p -> true);
        tablaPacientes.setItems(pacientesFiltrados);

        txtBuscarPaciente.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.trim().isEmpty()) {
                pacientesFiltrados.setPredicate(p -> true);
            } else {
                String f = n.toLowerCase().trim();
                pacientesFiltrados.setPredicate(p ->
                    p.getNombreCompleto().toLowerCase().contains(f)
                    || p.getDocumentoIdentidad().toLowerCase().contains(f));
            }
        });

        tablaPacientes.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                selectedPatientId = sel.getIdPaciente();
                selectedPatientName = sel.getNombreCompleto();
            } else {
                selectedPatientId = -1;
                selectedPatientName = "";
            }
            aplicarFiltroPendientes();
        });
    }

    private void aplicarFiltroPendientes() {
        int pid = selectedPatientId;
        String tc = txtBuscarCita.getText();
        String tt = txtBuscarTodos.getText();

        citasFiltradas.setPredicate(p -> filtrar(p, pid, tc));
        todosFiltrados.setPredicate(p -> filtrar(p, pid, tt));
    }

    private boolean filtrar(PendienteDTO p, int idPaciente, String texto) {
        if (idPaciente > 0 && p.getIdPaciente() != idPaciente) return false;
        if (texto == null || texto.trim().isEmpty()) return true;
        String f = texto.toLowerCase().trim();
        return p.getPacienteNombre().toLowerCase().contains(f)
            || p.getConcepto().toLowerCase().contains(f);
    }

    private void configurarTablaPendientes(TableColumn<PendienteDTO, Integer> colId,
                                           TableColumn<PendienteDTO, String> colPac,
                                           TableColumn<PendienteDTO, String> colConc,
                                           TableColumn<PendienteDTO, Double> colMont,
                                           TableColumn<PendienteDTO, String> colFec) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReferencia"));
        colPac.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colConc.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colMont.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colFec.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        colMont.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
    }

    private void configurarTablaFacturas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFactura"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaEmision"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colImpuesto.setCellValueFactory(new PropertyValueFactory<>("impuesto"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoPago"));

        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PAGADO".equals(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 600;");
                    } else if ("ANULADO".equals(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 600;");
                    }
                }
            }
        });

        colSubtotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colImpuesto.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        facturasList = FXCollections.observableArrayList();
        tablaFacturas.setItems(facturasList);

        tablaFacturas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                FacturaResumenDTO factura = tablaFacturas.getSelectionModel().getSelectedItem();
                if (factura != null) abrirDialogoEditarEstado(factura);
            }
        });

        // Context menu for ADMIN only on facturas table
        if ("ADMIN".equals(currentRole)) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem eliminarItem = new MenuItem("Eliminar factura");
            eliminarItem.setOnAction(e -> {
                FacturaResumenDTO factura = tablaFacturas.getSelectionModel().getSelectedItem();
                if (factura != null) confirmarEliminarFactura(factura);
            });
            contextMenu.getItems().add(eliminarItem);
            tablaFacturas.setContextMenu(contextMenu);
        }
    }

    private void confirmarEliminarFactura(FacturaResumenDTO factura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar factura");
        alert.setHeaderText("¿Está seguro de eliminar la factura #" + factura.getIdFactura() + "?");
        alert.setContentText("Los conceptos volverán a estar disponibles para facturar.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (facturaDAO.eliminarFactura(factura.getIdFactura())) {
                    mostrarAlerta("Éxito", "Factura eliminada. Los conceptos están disponibles de nuevo.", Alert.AlertType.INFORMATION);
                    cargarPendientes();
                    cargarFacturas();
                    aplicarFiltroPendientes();
                    EventBus.getInstance().post(new DatosFacturablesActualizadosEvent());
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la factura.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // Context menu for pending items (ADMIN/SECRETARIA only)
    private void configurarContextMenuPendientes(TableView<PendienteDTO> table) {
        if ("PACIENTE".equals(currentRole)) return;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem eliminarItem = new MenuItem("Eliminar pendiente");
        eliminarItem.setOnAction(e -> {
            PendienteDTO item = table.getSelectionModel().getSelectedItem();
            if (item != null) confirmarEliminarPendiente(item);
        });
        contextMenu.getItems().add(eliminarItem);
        table.setContextMenu(contextMenu);
    }

    private void confirmarEliminarPendiente(PendienteDTO item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar pendiente");
        alert.setHeaderText("¿Marcar como facturado y eliminar de pendientes?");
        alert.setContentText(item.getConcepto() + " - $" + String.format("%.2f", item.getMonto()));
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    boolean ok = consultaDAO.marcarFacturado(item.getIdReferencia());
                    if (ok) {
                        mostrarAlerta("Éxito", "Pendiente eliminado.", Alert.AlertType.INFORMATION);
                        cargarPendientes();
                        aplicarFiltroPendientes();
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el pendiente.", Alert.AlertType.ERROR);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarAlerta("Error", "No se pudo eliminar el pendiente.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void abrirDialogoEditarEstado(FacturaResumenDTO factura) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cambiar estado de factura #" + factura.getIdFactura());
        dialog.setHeaderText("Paciente: " + factura.getPacienteNombre());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("PENDIENTE", "PAGADO", "ANULADO");
        cbEstado.setValue(factura.getEstadoPago());
        cbEstado.setPrefWidth(250);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.add(new Label("Estado de pago:"), 0, 0);
        grid.add(cbEstado, 1, 0);

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> {
            String nuevoEstado = cbEstado.getValue();
            if (nuevoEstado == null) return;
            if (facturaDAO.actualizarEstadoPago(factura.getIdFactura(), nuevoEstado)) {
                mostrarAlerta("Éxito", "Estado actualizado a " + nuevoEstado, Alert.AlertType.INFORMATION);
                dialog.close();
                cargarFacturas();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
            }
        });

        VBox content = new VBox(12, grid, btnGuardar);
        content.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.showAndWait();
    }

    public void refrescar() {
        cargarPendientes();
        cargarFacturas();
        aplicarFiltroPendientes();
    }

    private void cargarPendientes() {
        if ("PACIENTE".equals(currentRole)) {
            int idPaciente = pacienteDAO.obtenerIdPacientePorIdPersona(currentIdPersona);
            if (idPaciente > 0) {
                citasPendientes.setAll(consultaDAO.listarPendientesConPaciente().stream()
                    .filter(p -> p.getIdPaciente() == idPaciente).collect(Collectors.toList()));
            } else {
                citasPendientes.clear();
            }
        } else {
            citasPendientes.setAll(consultaDAO.listarPendientesConPaciente());
        }
        List<PendienteDTO> todos = new java.util.ArrayList<>(citasPendientes);
        todos.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        todosPendientes.setAll(todos);

        ContextMenu cmCitas = new ContextMenu();
        if (!"PACIENTE".equals(currentRole)) {
            MenuItem eliminarCita = new MenuItem("Eliminar pendiente");
            eliminarCita.setOnAction(e -> {
                PendienteDTO item = tablaCitasPendientes.getSelectionModel().getSelectedItem();
                if (item != null) confirmarEliminarPendiente(item);
            });
            cmCitas.getItems().add(eliminarCita);
        }
        tablaCitasPendientes.setContextMenu(cmCitas);

        ContextMenu cmTodos = new ContextMenu();
        if (!"PACIENTE".equals(currentRole)) {
            MenuItem eliminarTodo = new MenuItem("Eliminar pendiente");
            eliminarTodo.setOnAction(e -> {
                PendienteDTO item = tablaTodosPendientes.getSelectionModel().getSelectedItem();
                if (item != null) confirmarEliminarPendiente(item);
            });
            cmTodos.getItems().add(eliminarTodo);
        }
        tablaTodosPendientes.setContextMenu(cmTodos);
    }

    private void cargarPacientes() {
        pacientesList.setAll(pacienteDAO.listarTodos());
    }

    private void configurarTablaTodos(TableColumn<PendienteDTO, Integer> colId,
                                      TableColumn<PendienteDTO, String> colPac,
                                      TableColumn<PendienteDTO, String> colConc,
                                      TableColumn<PendienteDTO, Double> colMont,
                                      TableColumn<PendienteDTO, String> colFec,
                                      TableColumn<PendienteDTO, String> colTipo) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReferencia"));
        colPac.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colConc.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colMont.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colFec.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoReferencia"));

        colMont.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colTipo.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                if ("CONSULTA".equals(item)) setStyle("-fx-text-fill: #0d9488; -fx-font-weight: 600;");
                else setStyle("-fx-text-fill: #d97706; -fx-font-weight: 600;");
            }
        });
    }

    private void cargarFacturas() {
        if ("PACIENTE".equals(currentRole)) {
            int idPaciente = pacienteDAO.obtenerIdPacientePorIdPersona(currentIdPersona);
            if (idPaciente > 0) {
                facturasList.setAll(facturaDAO.listarPorIdPaciente(idPaciente));
            } else {
                facturasList.clear();
            }
        } else {
            facturasList.setAll(facturaDAO.listarTodasConPaciente());
        }
    }

    private void generarFactura(String tipo) {
        if (selectedPatientId < 0) {
            mostrarAlerta("Error", "Seleccione un paciente de la tabla izquierda.", Alert.AlertType.ERROR);
            return;
        }

        FacturaDTO preview = service.previsualizarFactura(selectedPatientId, tipo);
        if (preview == null) {
            mostrarAlerta("Sin pendientes", "No hay conceptos pendientes para facturar de este tipo.", Alert.AlertType.INFORMATION);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Paciente: ").append(selectedPatientName).append("\n\n");
        sb.append("Conceptos pendientes").append(tipo == null ? " (todos)" : " (" + tipo.toLowerCase() + ")").append(":\n");
        preview.getDetalles().forEach(d ->
            sb.append(String.format("  - %s: $%.2f\n", d.getConcepto(), d.getMonto()))
        );
        sb.append(String.format("\nSubtotal: $%.2f\n", preview.getSubtotal()));
        sb.append(String.format("Impuesto (19%%): $%.2f\n", preview.getImpuesto()));
        sb.append(String.format("Total: $%.2f\n", preview.getTotal()));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Previsualización de factura");
        dialog.setHeaderText("Confirme los conceptos a facturar");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/esperanza/hopecare/main/hopecare.css").toExternalForm());

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setPrefHeight(300);
        area.setStyle("-fx-font-family: monospace;");

        VBox content = new VBox(10, area);
        content.setStyle("-fx-padding: 15;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResultConverter(btn -> btn);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                FacturaDTO factura = service.generarFactura(selectedPatientId, tipo);
                if (factura == null) {
                    mostrarAlerta("Error", "No se pudo generar la factura.", Alert.AlertType.ERROR);
                    return;
                }
                StringBuilder res = new StringBuilder();
                res.append("Factura generada exitosamente\n\n");
                res.append(String.format("Subtotal: $%.2f\n", factura.getSubtotal()));
                res.append(String.format("Impuesto (19%%): $%.2f\n", factura.getImpuesto()));
                res.append(String.format("Total: $%.2f\n\n", factura.getTotal()));
                res.append("Detalles:\n");
                factura.getDetalles().forEach(d ->
                    res.append(String.format(" - %s: $%.2f\n", d.getConcepto(), d.getMonto()))
                );
                mostrarAlerta("Factura generada", res.toString(), Alert.AlertType.INFORMATION);
                cargarPendientes();
                aplicarFiltroPendientes();
                cargarFacturas();
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

    private void mostrarAyuda() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ayuda / FAQ - Módulo de Facturación");
        dialog.setHeaderText("Preguntas frecuentes y guía de uso");

        String contenido = "GESTIÓN DE FACTURAS\n\n" +
                "• ¿Cómo crear una factura?\n" +
                "  1. Seleccione un paciente de la tabla izquierda\n" +
                "  2. Vaya a la pestaña 'Todos' o 'Consultas'\n" +
                "  3. Haga clic en 'Generar Factura'\n" +
                "  4. Confirme los conceptos y haga clic en ACEPTAR\n\n" +
                "• ¿Cómo eliminar una factura?\n" +
                "  - Solo el rol ADMIN puede eliminar facturas\n" +
                "  - Haga clic derecho sobre una factura → 'Eliminar factura'\n" +
                "  - Los conceptos vuelven a estar disponibles para facturar\n\n" +
                "• ¿Cómo cambio el estado de una factura?\n" +
                "  - Doble clic sobre la factura → seleccione el estado (PENDIENTE/PAGADO/ANULADO)\n\n" +
                "• ¿Cómo eliminar un pendiente?\n" +
                "  - Clic derecho sobre el pendiente → 'Eliminar pendiente'\n" +
                "  - Esto marca el concepto como facturado sin crear factura\n\n" +
                "ROLES Y PERMISOS\n\n" +
                "• ADMIN: Acceso completo (crear, editar, eliminar facturas)\n" +
                "• SECRETARIA: Crear facturas y pendientes, pero NO puede eliminar facturas\n" +
                "• PACIENTE: Solo visualiza sus propias facturas (modo lectura)\n\n" +
                "CONSEJOS\n\n" +
                "• Use el buscador para filtrar pacientes o conceptos\n" +
                "• La pestaña 'Todos' muestra todos los pendientes ordenados por fecha\n" +
                "• La pestaña 'Consultas' muestra solo consultas médicas pendientes";

        TextArea area = new TextArea(contenido);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(25);
        area.setPrefColumnCount(60);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        VBox content = new VBox(10, area);
        content.setStyle("-fx-padding: 15;");
        dialog.getDialogPane().setContent(content);

        ButtonType cerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(cerrar);

        dialog.showAndWait();
    }
}
