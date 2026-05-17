package com.esperanza.hopecare.modules.citas_consultas.view;

import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Consulta;
import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ConsultaPanel extends JPanel {
    private JComboBox<String> cbCitasPendientes;
    private JTextArea txtSintomas, txtDiagnostico, txtTratamiento;
    private JTextField txtPrecio;
    private JButton btnAtender, btnSolicitarExamen, btnRecetar;
    private CitaDAO citaDAO;
    private ConsultaDAO consultaDAO;
    private GestionClinicaFacade facade;

    public ConsultaPanel() {
        citaDAO = new CitaDAO();
        consultaDAO = new ConsultaDAO();
        facade = new GestionClinicaFacade();
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Cita programada:"));
        cbCitasPendientes = new JComboBox<>();
        top.add(cbCitasPendientes);
        btnAtender = new JButton("Atender y guardar consulta");
        top.add(btnAtender);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(4, 1, 5, 5));
        txtSintomas = new JTextArea(5, 40);
        txtDiagnostico = new JTextArea(5, 40);
        txtTratamiento = new JTextArea(5, 40);
        txtPrecio = new JTextField(10);
        center.add(new JScrollPane(txtSintomas) {{ setBorder(BorderFactory.createTitledBorder("Síntomas")); }});
        center.add(new JScrollPane(txtDiagnostico) {{ setBorder(BorderFactory.createTitledBorder("Diagnóstico")); }});
        center.add(new JScrollPane(txtTratamiento) {{ setBorder(BorderFactory.createTitledBorder("Tratamiento")); }});
        center.add(new JPanel(new FlowLayout()) {{ add(new JLabel("Precio ($):")); add(txtPrecio); setBorder(BorderFactory.createTitledBorder("Precio")); }});
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        btnSolicitarExamen = new JButton("Solicitar examen");
        btnRecetar = new JButton("Recetar medicamento");
        bottom.add(btnSolicitarExamen);
        bottom.add(btnRecetar);
        add(bottom, BorderLayout.SOUTH);

        cargarCitasPendientes();

        btnAtender.addActionListener(e -> atenderConsulta());
        btnSolicitarExamen.addActionListener(e -> solicitarExamen());
        btnRecetar.addActionListener(e -> recetarMedicamento());
    }

    private void cargarCitasPendientes() {
        cbCitasPendientes.removeAllItems();
        List<Cita> citas = citaDAO.obtenerCitasPorEstado("PROGRAMADA");
        for (Cita c : citas) {
            cbCitasPendientes.addItem(c.getIdCita() + " - Paciente: " + c.getIdPaciente() + " - " + c.getFechaHora());
        }
    }

    private void atenderConsulta() {
        if (cbCitasPendientes.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una cita");
            return;
        }
        String selected = (String) cbCitasPendientes.getSelectedItem();
        int idCita = Integer.parseInt(selected.split(" - ")[0]);
        String sintomas = txtSintomas.getText().trim();
        String diagnostico = txtDiagnostico.getText().trim();
        String tratamiento = txtTratamiento.getText().trim();

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            precio = 0.0;
        }
        if (precio < 0) {
            JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.");
            return;
        }
        Consulta consulta = new Consulta(idCita, diagnostico, sintomas, tratamiento, false, precio);
        int idConsulta = consultaDAO.insertarConsultaYActualizarEstado(consulta);
        if (idConsulta > 0) {
            JOptionPane.showMessageDialog(this, "Consulta registrada exitosamente");
            cargarCitasPendientes();
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar consulta");
        }
    }

    private void solicitarExamen() {
        JOptionPane.showMessageDialog(this, "Funcionalidad: abrir diálogo para seleccionar examen y asociar a consulta");
    }

    private void recetarMedicamento() {
        JOptionPane.showMessageDialog(this, "Funcionalidad: abrir diálogo para seleccionar medicamento y cantidad");
    }

    private void limpiarCampos() {
        txtSintomas.setText("");
        txtDiagnostico.setText("");
        txtTratamiento.setText("");
        txtPrecio.setText("");
    }
}
