package com.esperanza.hopecare.ui.farmacia;

import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.MedicamentoDAO;
import com.esperanza.hopecare.modules.medicamentos_lab.model.Medicamento;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Paciente;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FarmaciaPanel extends JPanel {
    private JComboBox<Paciente> cbPacientes;
    private JComboBox<Medicamento> cbMedicamentos;
    private JSpinner spCantidad;
    private JCheckBox chkPresentoReceta;
    private JButton btnEntregar;
    private GestionClinicaFacade facade;
    private MedicamentoDAO medicamentoDAO;
    private PacienteDAO pacienteDAO;

    public FarmaciaPanel() {
        facade = new GestionClinicaFacade();
        medicamentoDAO = new MedicamentoDAO();
        pacienteDAO = new PacienteDAO();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Paciente:"), gbc);
        cbPacientes = new JComboBox<>();
        gbc.gridx=1; add(cbPacientes, gbc);

        gbc.gridx=0; gbc.gridy=1; add(new JLabel("Medicamento:"), gbc);
        cbMedicamentos = new JComboBox<>();
        gbc.gridx=1; add(cbMedicamentos, gbc);

        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Cantidad:"), gbc);
        spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        gbc.gridx=1; add(spCantidad, gbc);

        gbc.gridx=0; gbc.gridy=3; add(new JLabel("Presentó receta:"), gbc);
        chkPresentoReceta = new JCheckBox();
        gbc.gridx=1; add(chkPresentoReceta, gbc);

        btnEntregar = new JButton("Registrar entrega");
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2;
        add(btnEntregar, gbc);

        cargarPacientes();
        cargarMedicamentos();

        btnEntregar.addActionListener(e -> entregar());
    }

    private void cargarPacientes() {
        cbPacientes.removeAllItems();
        List<Paciente> pacientes = pacienteDAO.listarTodos();
        for (Paciente p : pacientes) {
            cbPacientes.addItem(p);
        }
    }

    private void cargarMedicamentos() {
        cbMedicamentos.removeAllItems();
        List<Medicamento> medicamentos = medicamentoDAO.listarTodos();
        for (Medicamento m : medicamentos) {
            cbMedicamentos.addItem(m);
        }
    }

    private void entregar() {
        if (cbPacientes.getSelectedIndex() == -1 || cbMedicamentos.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione paciente y medicamento");
            return;
        }
        Paciente paciente = (Paciente) cbPacientes.getSelectedItem();
        int idPaciente = paciente.getIdPaciente();
        Medicamento med = (Medicamento) cbMedicamentos.getSelectedItem();
        int cantidad = (Integer) spCantidad.getValue();
        boolean presenteReceta = chkPresentoReceta.isSelected();
        String rol = "FARMACIA";

        boolean ok = facade.procesarEntregaMedicamento(idPaciente, med.getIdMedicamento(), cantidad, presenteReceta, rol);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Entrega registrada y stock actualizado");
            cargarMedicamentos();
        } else {
            JOptionPane.showMessageDialog(this, "Error: stock insuficiente o datos inválidos");
        }
    }
}