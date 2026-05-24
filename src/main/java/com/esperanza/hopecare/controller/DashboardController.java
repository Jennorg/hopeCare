package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.dao.DashboardDAO;
import com.esperanza.hopecare.model.DashboardData;
import com.esperanza.hopecare.model.RegistroItem;
import com.esperanza.hopecare.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private Label lblCitasHoy;
    @FXML private Label lblAtendidosHoy;
    @FXML private Label lblIngresosMes;
    @FXML private Label lblAsistencia;
    @FXML private ProgressBar barAsistencia;

    @FXML private Label lblProgramadas;
    @FXML private Label lblAtendidas;
    @FXML private Label lblCanceladas;
    @FXML private Label lblNoAsistio;

    @FXML private Label lblStockBajo;
    @FXML private Label lblLabPendientes;
    @FXML private Label lblFacturasPendientes;

    @FXML private Label lblIngConsultas;
    @FXML private Label lblIngFarmacia;
    @FXML private Label lblIngLaboratorio;

    @FXML private ListView<RegistroItem> listRegistros;
    @FXML private ListView<RegistroItem> listPacientes;
    @FXML private ListView<RegistroItem> listMedicos;

    @FXML private Label lblTotalPacientes;
    @FXML private Label lblTotalMedicos;
    @FXML private Label lblTotalMedicamentos;
    @FXML private Label lblTotalExamenes;

    @FXML private VBox cardCitasHoy;
    @FXML private VBox cardAtendidos;
    @FXML private VBox cardIngresos;
    @FXML private VBox cardFacturasPend;
    @FXML private VBox cardConsultas;

    private DashboardDAO dao;

    @FXML
    public void initialize() {
        dao = new DashboardDAO();

        listRegistros.setCellFactory(v -> crearCeldaRegistro());
        listPacientes.setCellFactory(v -> crearCeldaRegistro());
        listMedicos.setCellFactory(v -> crearCeldaRegistro());

        Platform.runLater(this::refrescar);

        listRegistros.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                RegistroItem selected = listRegistros.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    mostrarDetalleRegistro(selected);
                }
            }
        });

        listPacientes.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                RegistroItem selected = listPacientes.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    mostrarDetalleEliminarPacienteMedico(selected);
                }
            }
        });

        listMedicos.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                RegistroItem selected = listMedicos.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    mostrarDetalleEliminarPacienteMedico(selected);
                }
            }
        });

        EventBus.getInstance().register(NuevaCitaEvent.class, event -> Platform.runLater(this::refrescar));
        EventBus.getInstance().register(NuevaConsultaEvent.class, event -> Platform.runLater(this::refrescar));
        EventBus.getInstance().register(DatosFacturablesActualizadosEvent.class, event -> Platform.runLater(this::refrescar));
    }

    private void refrescar() {
        DashboardData data = dao.cargarTodosLosDatos();

        lblCitasHoy.setText(String.valueOf(data.getCitasHoy()));
        lblAtendidosHoy.setText(String.valueOf(data.getPacientesAtendidosHoy()));
        lblIngresosMes.setText(String.format("$%,.0f", data.getIngresosMes()));

        double pct = data.getPorcentajeAsistencia();
        lblAsistencia.setText(String.format("%.0f%%", pct));
        barAsistencia.setProgress(pct / 100.0);

        var estados = data.getEstadoCitas();
        lblProgramadas.setText(String.valueOf(estados.getOrDefault("PROGRAMADA", 0)));
        lblAtendidas.setText(String.valueOf(estados.getOrDefault("ATENDIDA", 0)));
        lblCanceladas.setText(String.valueOf(estados.getOrDefault("CANCELADA", 0)));
        lblNoAsistio.setText(String.valueOf(estados.getOrDefault("NO_ASISTIO", 0)));

        int stockCount = data.getMedicamentosStockBajo().size();
        lblStockBajo.setText(stockCount + " medicamentos");
        lblLabPendientes.setText(data.getSolicitudesLabPendientes() + " solicitudes");
        lblFacturasPendientes.setText(data.getFacturasPendientes() + " facturas");

        lblIngConsultas.setText(String.format("$%,.0f", data.getIngresosConsultas()));
        lblIngFarmacia.setText(String.format("$%,.0f", data.getIngresosFarmacia()));
        lblIngLaboratorio.setText(String.format("$%,.0f", data.getIngresosLaboratorio()));

        ObservableList<RegistroItem> registros = data.getRegistrosRecientes() != null
            ? FXCollections.observableArrayList(data.getRegistrosRecientes())
            : FXCollections.observableArrayList();
        listRegistros.setItems(registros);

        if (data.getListaPacientes() != null) listPacientes.getItems().setAll(data.getListaPacientes());
        if (data.getListaMedicos() != null) listMedicos.getItems().setAll(data.getListaMedicos());

        lblTotalPacientes.setText(String.valueOf(data.getTotalPacientes()));
        lblTotalMedicos.setText(String.valueOf(data.getTotalMedicos()));
        lblTotalMedicamentos.setText(String.valueOf(data.getTotalMedicamentos()));
        lblTotalExamenes.setText(String.valueOf(data.getTotalExamenes()));
    }

    private void mostrarDetalleEliminarPacienteMedico(RegistroItem item) {
        String[] detalles;
        String titulo;
        String tipo;
        boolean esPaciente = "PACIENTE".equals(item.getTipo());

        if (esPaciente) {
            detalles = dao.obtenerDetalleCompletoPaciente(item.getId());
            titulo = "Detalle de Paciente";
            tipo = "PACIENTE";
        } else {
            detalles = dao.obtenerDetalleCompletoMedico(item.getId());
            titulo = "Detalle de M\u00e9dico";
            tipo = "M\u00c9DICO";
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        VBox content = new VBox(4);
        content.setStyle("-fx-padding: 16;");

        for (String linea : detalles) {
            Label lbl = new Label(linea);
            if (linea.equals("PACIENTE") || linea.equals("M\u00c9DICO")) {
                lbl.setStyle("-fx-font-weight: 700; -fx-font-size: 16px; -fx-text-fill: #0f172a; -fx-padding: 0 0 8 0;");
            } else {
                lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
            }
            content.getChildren().add(lbl);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setMaxHeight(500);

        dialog.getDialogPane().setContent(scroll);
        ButtonType btnEliminar = new ButtonType("Eliminar " + tipo, ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(btnEliminar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(500);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnEliminar) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "\u00bfEst\u00e1 seguro de eliminar este " + tipo.toLowerCase() + "?",
                    ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirmar eliminaci\u00f3n");
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.YES) {
                        boolean ok;
                        if (esPaciente) {
                            ok = dao.eliminarPaciente(item.getId());
                        } else {
                            ok = dao.eliminarMedico(item.getId());
                        }
                        if (ok) {
                            Alert info = new Alert(Alert.AlertType.INFORMATION,
                                tipo + " eliminado correctamente.");
                            info.setHeaderText(null);
                            info.showAndWait();
                            refrescar();
                        } else {
                            Alert err = new Alert(Alert.AlertType.ERROR,
                                "No se pudo eliminar. El " + tipo.toLowerCase() +
                                " tiene registros asociados.");
                            err.setHeaderText(null);
                            err.showAndWait();
                        }
                    }
                });
            }
        });
    }

    private void mostrarDetalleRegistro(RegistroItem item) {
        String[] detalles;
        switch (item.getTipo()) {
            case "CITA":
                detalles = dao.obtenerDetalleCompletoCita(item.getId());
                mostrarDialogoDetalle("Detalle de Cita", detalles);
                break;
            case "CONSULTA":
                detalles = dao.obtenerDetalleCompletoConsulta(item.getId());
                mostrarDialogoDetalle("Detalle de Consulta", detalles);
                break;
            case "FACTURA":
                detalles = dao.obtenerDetalleCompletoFactura(item.getId());
                mostrarDialogoDetalle("Detalle de Factura", detalles);
                break;
        }
    }

    private void mostrarDialogoDetalle(String titulo, String[] lineas) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        VBox content = new VBox(4);
        content.setStyle("-fx-padding: 16;");

        for (String linea : lineas) {
            Label lbl = new Label(linea);
            if (linea.startsWith("---")) {
                lbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #0d9488; -fx-padding: 8 0 4 0;");
            } else if (linea.startsWith("CITA #") || linea.startsWith("CONSULTA #") || linea.startsWith("FACTURA #")) {
                lbl.setStyle("-fx-font-weight: 700; -fx-font-size: 16px; -fx-text-fill: #0f172a; -fx-padding: 0 0 8 0;");
            } else if (linea.startsWith("TOTAL:")) {
                lbl.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #0d9488;");
            } else if (linea.startsWith("  ")) {
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 0 0 0 12;");
            } else {
                lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
            }
            content.getChildren().add(lbl);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setMaxHeight(500);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(500);
        dialog.showAndWait();
    }

    @FXML
    private void mostrarDetalleCitasHoy() {
        java.util.List<String[]> datos = dao.obtenerDetalleCitasHoy();
        mostrarDialogoLista("Citas de Hoy", new String[]{"Paciente", "M\u00e9dico", "Hora", "Estado", "Motivo"}, datos);
    }

    @FXML
    private void mostrarDetalleAtendidos() {
        java.util.List<String[]> datos = dao.obtenerDetalleAtendidosHoy();
        mostrarDialogoLista("Atendidos Hoy", new String[]{"Paciente", "Hora", "Diagn\u00f3stico"}, datos);
    }

    @FXML
    private void mostrarDetalleFacturas() {
        java.util.List<String[]> datos = dao.obtenerDetalleFacturas();
        mostrarDialogoLista("Facturas", new String[]{"Factura", "Paciente", "Fecha", "Total", "Estado", "Pago"}, datos);
    }

    @FXML
    private void mostrarDetalleConsultas() {
        java.util.List<String[]> datos = dao.obtenerDetalleConsultas();
        mostrarDialogoLista("Consultas Realizadas", new String[]{"Paciente", "M\u00e9dico", "Fecha", "Diagn\u00f3stico", "Precio"}, datos);
    }

    private ListCell<RegistroItem> crearCeldaRegistro() {
        return new ListCell<RegistroItem>() {
            @Override
            protected void updateItem(RegistroItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getDisplayText());
                }
            }
        };
    }

    private void mostrarDialogoLista(String titulo, String[] headers, java.util.List<String[]> datos) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);

        VBox content = new VBox(6);
        content.setStyle("-fx-padding: 16;");

        Label headerLine = new Label(String.join("  |  ", headers));
        headerLine.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #0d9488; -fx-padding: 0 0 8 0;");
        content.getChildren().add(headerLine);

        if (datos.isEmpty()) {
            content.getChildren().add(new Label("No hay datos disponibles."));
        } else {
            ScrollPane scroll = new ScrollPane();
            VBox rows = new VBox(4);
            for (String[] fila : datos) {
                rows.getChildren().add(new Label(String.join("  |  ", fila)));
            }
            scroll.setContent(rows);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(300);
            scroll.setMaxHeight(400);
            content.getChildren().add(scroll);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinWidth(600);
        dialog.showAndWait();
    }
}
