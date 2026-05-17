package com.esperanza.hopecare.modules.dashboard.ui;

import com.esperanza.hopecare.common.events.*;
import com.esperanza.hopecare.modules.dashboard.dao.DashboardDAO;
import com.esperanza.hopecare.modules.dashboard.model.DashboardData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;

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

    @FXML private ListView<String> listRegistros;

    @FXML private Label lblTotalPacientes;
    @FXML private Label lblTotalMedicos;
    @FXML private Label lblTotalMedicamentos;
    @FXML private Label lblTotalExamenes;

    private DashboardDAO dao;

    @FXML
    public void initialize() {
        dao = new DashboardDAO();
        refrescar();

        EventBus.getInstance().register(NuevaCitaEvent.class, event -> Platform.runLater(this::refrescar));
        EventBus.getInstance().register(NuevaConsultaEvent.class, event -> Platform.runLater(this::refrescar));
        EventBus.getInstance().register(NuevaFacturaEvent.class, event -> Platform.runLater(this::refrescar));
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

        ObservableList<String> registros = FXCollections.observableArrayList(data.getRegistrosRecientes());
        listRegistros.setItems(registros);

        lblTotalPacientes.setText(String.valueOf(data.getTotalPacientes()));
        lblTotalMedicos.setText(String.valueOf(data.getTotalMedicos()));
        lblTotalMedicamentos.setText(String.valueOf(data.getTotalMedicamentos()));
        lblTotalExamenes.setText(String.valueOf(data.getTotalExamenes()));
    }
}
