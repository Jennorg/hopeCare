package com.esperanza.hopecare.util;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardView extends JFrame {
    private JLabel lblCitasHoy;
    private JLabel lblIngresosMes;
    private JList<String> listMedicamentosBajoStock;

    public DashboardView() {
        setTitle("Panel de Control - Sisgeho");
        setLayout(new GridLayout(3, 1));
        lblCitasHoy = new JLabel("Citas hoy: --");
        lblIngresosMes = new JLabel("Ingresos del mes: --");
        listMedicamentosBajoStock = new JList<>();
        add(lblCitasHoy);
        add(lblIngresosMes);
        add(new JScrollPane(listMedicamentosBajoStock));
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void actualizarCitasDelDia(int cantidad) {
        SwingUtilities.invokeLater(() -> lblCitasHoy.setText("Citas hoy: " + cantidad));
    }

    public void actualizarIngresosMensuales(double monto) {
        SwingUtilities.invokeLater(() -> lblIngresosMes.setText(String.format("Ingresos del mes: $%.2f", monto)));
    }

    public void actualizarMedicamentosBajoStock(List<String> medicamentos) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<String> model = new DefaultListModel<>();
            medicamentos.forEach(model::addElement);
            listMedicamentosBajoStock.setModel(model);
        });
    }
}
