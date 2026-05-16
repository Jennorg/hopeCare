package com.esperanza.hopecare.facturacion.view;

import com.esperanza.hopecare.common.events.DatosFacturablesActualizadosEvent;
import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.modules.facturacion.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.facturacion.dao.EntregaMedicamentoDAO;
import com.esperanza.hopecare.modules.facturacion.dao.FacturaDAO;
import com.esperanza.hopecare.modules.facturacion.dao.SolicitudExamenDAO;
import com.esperanza.hopecare.modules.facturacion.dto.FacturaDTO;
import com.esperanza.hopecare.modules.facturacion.dto.FacturaResumenDTO;
import com.esperanza.hopecare.modules.facturacion.dto.PendienteDTO;
import com.esperanza.hopecare.modules.facturacion.service.FacturacionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FacturacionController {
    @FXML private TableView<FacturaResumenDTO> tablaFacturas;
    @FXML private TableColumn<FacturaResumenDTO, Integer> colId;
    @FXML private TableColumn<FacturaResumenDTO, String> colPaciente;
    @FXML private TableColumn<FacturaResumenDTO, String> colFecha;
    @FXML private TableColumn<FacturaResumenDTO, Double> colSubtotal;
    @FXML private TableColumn<FacturaResumenDTO, Double> colImpuesto;
    @FXML private TableColumn<FacturaResumenDTO, Double> colTotal;
    @FXML private TableColumn<FacturaResumenDTO, String> colEstado;

    @FXML private TextField txtBuscarCita;
    @FXML private Button btnGenCita;
    @FXML private TableView<PendienteDTO> tablaCitasPendientes;
    @FXML private TableColumn<PendienteDTO, Integer> colCitaIdRef;
    @FXML private TableColumn<PendienteDTO, String> colCitaPaciente;
    @FXML private TableColumn<PendienteDTO, String> colCitaConcepto;
    @FXML private TableColumn<PendienteDTO, Double> colCitaMonto;
    @FXML private TableColumn<PendienteDTO, String> colCitaFecha;

    @FXML private TextField txtBuscarFarm;
    @FXML private Button btnGenFarm;
    @FXML private TableView<PendienteDTO> tablaFarmPendientes;
    @FXML private TableColumn<PendienteDTO, Integer> colFarmIdRef;
    @FXML private TableColumn<PendienteDTO, String> colFarmPaciente;
    @FXML private TableColumn<PendienteDTO, String> colFarmConcepto;
    @FXML private TableColumn<PendienteDTO, Double> colFarmMonto;
    @FXML private TableColumn<PendienteDTO, String> colFarmFecha;

    @FXML private TextField txtBuscarLab;
    @FXML private Button btnGenLab;
    @FXML private TableView<PendienteDTO> tablaLabPendientes;
    @FXML private TableColumn<PendienteDTO, Integer> colLabIdRef;
    @FXML private TableColumn<PendienteDTO, String> colLabPaciente;
    @FXML private TableColumn<PendienteDTO, String> colLabConcepto;
    @FXML private TableColumn<PendienteDTO, Double> colLabMonto;
    @FXML private TableColumn<PendienteDTO, String> colLabFecha;

    private FacturacionService service;
    private FacturaDAO facturaDAO;
    private ObservableList<FacturaResumenDTO> facturasList;

    private ObservableList<PendienteDTO> citasPendientes;
    private ObservableList<PendienteDTO> farmPendientes;
    private ObservableList<PendienteDTO> labPendientes;
    private FilteredList<PendienteDTO> citasFiltradas;
    private FilteredList<PendienteDTO> farmFiltradas;
    private FilteredList<PendienteDTO> labFiltradas;

    private ConsultaDAO consultaDAO;
    private EntregaMedicamentoDAO entregaDAO;
    private SolicitudExamenDAO solicitudExamenDAO;

    @FXML
    public void initialize() {
        service = new FacturacionService();
        facturaDAO = new FacturaDAO();
        consultaDAO = new ConsultaDAO();
        entregaDAO = new EntregaMedicamentoDAO();
        solicitudExamenDAO = new SolicitudExamenDAO();

        configurarTablaPendientes(colCitaIdRef, colCitaPaciente, colCitaConcepto, colCitaMonto, colCitaFecha);
        configurarTablaPendientes(colFarmIdRef, colFarmPaciente, colFarmConcepto, colFarmMonto, colFarmFecha);
        configurarTablaPendientes(colLabIdRef, colLabPaciente, colLabConcepto, colLabMonto, colLabFecha);

        citasPendientes = FXCollections.observableArrayList();
        farmPendientes = FXCollections.observableArrayList();
        labPendientes = FXCollections.observableArrayList();

        citasFiltradas = new FilteredList<>(citasPendientes, p -> true);
        farmFiltradas = new FilteredList<>(farmPendientes, p -> true);
        labFiltradas = new FilteredList<>(labPendientes, p -> true);

        tablaCitasPendientes.setItems(citasFiltradas);
        tablaFarmPendientes.setItems(farmFiltradas);
        tablaLabPendientes.setItems(labFiltradas);

        txtBuscarCita.textProperty().addListener((obs, o, n) ->
            citasFiltradas.setPredicate(p -> filtrar(p, n)));
        txtBuscarFarm.textProperty().addListener((obs, o, n) ->
            farmFiltradas.setPredicate(p -> filtrar(p, n)));
        txtBuscarLab.textProperty().addListener((obs, o, n) ->
            labFiltradas.setPredicate(p -> filtrar(p, n)));

        btnGenCita.setOnAction(e -> generarFactura(tablaCitasPendientes, "CONSULTA"));
        btnGenFarm.setOnAction(e -> generarFactura(tablaFarmPendientes, "MEDICAMENTO"));
        btnGenLab.setOnAction(e -> generarFactura(tablaLabPendientes, "EXAMEN"));

        configurarTablaFacturas();
        try {
            cargarFacturas();
            cargarPendientes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        EventBus.getInstance().register(DatosFacturablesActualizadosEvent.class, e -> refrescar());
    }

    private boolean filtrar(PendienteDTO p, String texto) {
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
    }

    private void abrirDialogoEditarEstado(FacturaResumenDTO factura) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cambiar estado de factura #" + factura.getIdFactura());
        dialog.setHeaderText("Paciente: " + factura.getPacienteNombre());

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
    }

    private void cargarPendientes() {
        citasPendientes.setAll(consultaDAO.listarPendientesConPaciente());
        farmPendientes.setAll(entregaDAO.listarPendientesConPaciente());
        labPendientes.setAll(solicitudExamenDAO.listarPendientesConPaciente());
    }

    private void cargarFacturas() {
        facturasList.setAll(facturaDAO.listarTodasConPaciente());
    }

    private void generarFactura(TableView<PendienteDTO> tabla, String tipo) {
        PendienteDTO sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Error", "Seleccione un registro pendiente de la tabla.", Alert.AlertType.ERROR);
            return;
        }

        FacturaDTO preview = service.previsualizarFactura(sel.getIdPaciente(), tipo);
        if (preview == null) {
            mostrarAlerta("Sin pendientes", "No hay conceptos pendientes para facturar de este tipo.", Alert.AlertType.INFORMATION);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Paciente: ").append(sel.getPacienteNombre()).append("\n\n");
        sb.append("Conceptos pendientes (").append(tipo.toLowerCase()).append("):\n");
        preview.getDetalles().forEach(d ->
            sb.append(String.format("  - %s: $%.2f\n", d.getConcepto(), d.getMonto()))
        );
        sb.append(String.format("\nSubtotal: $%.2f\n", preview.getSubtotal()));
        sb.append(String.format("Impuesto (19%%): $%.2f\n", preview.getImpuesto()));
        sb.append(String.format("Total: $%.2f\n", preview.getTotal()));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Previsualización de factura");
        dialog.setHeaderText("Confirme los conceptos a facturar");

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
                FacturaDTO factura = service.generarFactura(sel.getIdPaciente(), tipo);
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
}
