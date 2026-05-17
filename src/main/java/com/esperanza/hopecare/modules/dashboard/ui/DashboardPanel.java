package com.esperanza.hopecare.modules.dashboard.ui;

import com.esperanza.hopecare.common.events.EventBus;
import com.esperanza.hopecare.common.events.NuevaCitaEvent;
import com.esperanza.hopecare.common.events.NuevaFacturaEvent;
import com.esperanza.hopecare.modules.dashboard.dao.DashboardDAO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private JLabel lblCitasHoy, lblIngresosMes;
    private JList<String> listStockBajo;
    private DefaultListModel<String> listModel;
    private DashboardDAO dao;
    
    public DashboardPanel() {
        dao = new DashboardDAO();
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(2,1));
        lblCitasHoy = new JLabel("Citas de hoy: --");
        lblIngresosMes = new JLabel("Ingresos del mes: --");
        top.add(lblCitasHoy);
        top.add(lblIngresosMes);
        add(top, BorderLayout.NORTH);
        listModel = new DefaultListModel<>();
        listStockBajo = new JList<>(listModel);
        add(new JScrollPane(listStockBajo), BorderLayout.CENTER);
        EventBus.getInstance().register(NuevaCitaEvent.class, this::onCitaNueva);
        EventBus.getInstance().register(NuevaFacturaEvent.class, this::onFacturaNueva);
        refrescar();
    }
    
    private void onCitaNueva(NuevaCitaEvent e) { SwingUtilities.invokeLater(() -> refrescar()); }
    private void onFacturaNueva(NuevaFacturaEvent e) { SwingUtilities.invokeLater(() -> refrescar()); }
    
    private void refrescar() {
        new SwingWorker<Void, Void>() {
            private int citas; private double ingresos; private List<String> meds;
            @Override protected Void doInBackground() {
                citas = dao.obtenerCitasDelDia();
                ingresos = dao.obtenerIngresosDelMes();
                meds = dao.obtenerMedicamentosStockBajo();
                return null;
            }
            @Override protected void done() {
                lblCitasHoy.setText("Citas de hoy: " + citas);
                lblIngresosMes.setText(String.format("Ingresos del mes: $%.2f", ingresos));
                listModel.clear();
                meds.forEach(listModel::addElement);
            }
        }.execute();
    }
}
