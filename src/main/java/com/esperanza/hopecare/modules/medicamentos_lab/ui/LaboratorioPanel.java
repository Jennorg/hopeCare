package com.esperanza.hopecare.modules.medicamentos_lab.ui;

import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;
import javax.swing.*;
import java.awt.*;

public class LaboratorioPanel extends JPanel {
    private JComboBox<String> cbSolicitudes;
    private JTextArea txtResultado;
    private JButton btnRegistrar;
    private GestionClinicaFacade facade;

    public LaboratorioPanel() {
        facade = new GestionClinicaFacade();
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Solicitud de examen:"));
        cbSolicitudes = new JComboBox<>();
        top.add(cbSolicitudes);
        add(top, BorderLayout.NORTH);

        txtResultado = new JTextArea(10, 40);
        add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        btnRegistrar = new JButton("Registrar resultado");
        add(btnRegistrar, BorderLayout.SOUTH);

        cargarSolicitudesPendientes();
        btnRegistrar.addActionListener(e -> registrarResultado());
    }

    private void cargarSolicitudesPendientes() {
        cbSolicitudes.addItem("Solicitud #1 - Hemograma (Paciente Juan)");
        cbSolicitudes.addItem("Solicitud #2 - Glucosa (Paciente María)");
    }

    private void registrarResultado() {
        if (cbSolicitudes.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud");
            return;
        }
        String solicitudStr = (String) cbSolicitudes.getSelectedItem();
        int idSolicitud = Integer.parseInt(solicitudStr.split("#")[1].split(" ")[0]);
        String resultado = txtResultado.getText().trim();
        if (resultado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el resultado");
            return;
        }
        boolean ok = facade.registrarResultadoExamen(idSolicitud, resultado, "COMPLETADO", "LABORATORIO");
        if (ok) {
            JOptionPane.showMessageDialog(this, "Resultado registrado");
            cargarSolicitudesPendientes();
            txtResultado.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar resultado");
        }
    }
}
