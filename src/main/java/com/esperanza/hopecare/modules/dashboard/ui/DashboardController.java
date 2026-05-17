package com.esperanza.hopecare.modules.dashboard.ui;

import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.NuevaCitaEvent;
import com.esperanza.hopecare.common.events.NuevaFacturaEvent;
import com.esperanza.hopecare.modules.dashboard.dao.DashboardDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class DashboardController {
    @FXML private Label lblCitasHoy;
    @FXML private Label lblIngresosMes;
    @FXML private ListView<String> listStockBajo;

    private DashboardDAO dao;
    private ObservableList<String> stockBajoList;

    @FXML
    public void initialize() {
        dao = new DashboardDAO();
        stockBajoList = FXCollections.observableArrayList();
        listStockBajo.setItems(stockBajoList);
        refrescar();

        // Suscripción a eventos
        EventBus.getInstance().register(NuevaCitaEvent.class, event -> {
            Platform.runLater(() -> refrescar());
        });
        EventBus.getInstance().register(NuevaFacturaEvent.class, event -> {
            Platform.runLater(() -> refrescar());
        });
    }

    private void refrescar() {
        int citas = dao.obtenerCitasDelDia();
        double ingresos = dao.obtenerIngresosDelMes();
        lblCitasHoy.setText("Citas de hoy: " + citas);
        lblIngresosMes.setText(String.format("Ingresos del mes: $%.2f", ingresos));

        stockBajoList.setAll(dao.obtenerMedicamentosStockBajo());
    }
}
