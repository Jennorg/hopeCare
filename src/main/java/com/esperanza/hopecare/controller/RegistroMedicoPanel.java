package com.esperanza.hopecare.controller;

import com.esperanza.hopecare.dao.MedicoDAO;
import com.esperanza.hopecare.model.Medico;
import javax.swing.*;
import java.awt.*;

public class RegistroMedicoPanel extends JPanel {
    private JTextField txtDocumento, txtRegistro;
    private JComboBox<String> cbEspecialidad;
    private JButton btnRegistrar;
    private MedicoDAO medicoDAO;
    
    public RegistroMedicoPanel() {
        medicoDAO = new MedicoDAO();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        
        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Documento:"), gbc);
        txtDocumento = new JTextField(15);
        gbc.gridx=1; add(txtDocumento, gbc);
        
        gbc.gridx=0; gbc.gridy=1; add(new JLabel("Especialidad:"), gbc);
        cbEspecialidad = new JComboBox<>(new String[]{"Medicina General", "Pediatría", "Traumatología"});
        gbc.gridx=1; add(cbEspecialidad, gbc);
        
        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Registro Médico:"), gbc);
        txtRegistro = new JTextField(15);
        gbc.gridx=1; add(txtRegistro, gbc);
        
        btnRegistrar = new JButton("Registrar Médico");
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        add(btnRegistrar, gbc);
        
        btnRegistrar.addActionListener(e -> registrar());
    }
    
    private void registrar() {
        String doc = txtDocumento.getText().trim();
        int idEsp = cbEspecialidad.getSelectedIndex() + 1;
        String reg = txtRegistro.getText().trim();
        if (doc.isEmpty() || reg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos");
            return;
        }
        Medico m = new Medico(doc, idEsp, reg);
        boolean ok = medicoDAO.insertarMedico(m);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Médico registrado con ID: " + m.getIdPersona());
            txtDocumento.setText("");
            txtRegistro.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar médico");
        }
    }
}
