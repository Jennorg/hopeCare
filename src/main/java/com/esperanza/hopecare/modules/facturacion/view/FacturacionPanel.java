package com.esperanza.hopecare.modules.facturacion.view;

import com.esperanza.hopecare.modules.facturacion.dto.FacturaDTO;
import com.esperanza.hopecare.modules.facturacion.service.FacturacionService;
import javax.swing.*;
import java.awt.*;

public class FacturacionPanel extends JPanel {
    private JTextField txtIdPaciente;
    private JTextArea txtResultado;
    private JButton btnGenerar;
    private FacturacionService service;
    
    public FacturacionPanel() {
        service = new FacturacionService();
        setLayout(new BorderLayout());
        
        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("ID Paciente:"));
        txtIdPaciente = new JTextField(10);
        top.add(txtIdPaciente);
        btnGenerar = new JButton("Generar Factura");
        top.add(btnGenerar);
        add(top, BorderLayout.NORTH);
        
        txtResultado = new JTextArea(15, 50);
        txtResultado.setEditable(false);
        add(new JScrollPane(txtResultado), BorderLayout.CENTER);
        
        btnGenerar.addActionListener(e -> generar());
    }
    
    private void generar() {
        try {
            int idPaciente = Integer.parseInt(txtIdPaciente.getText());
            FacturaDTO factura = service.generarFactura(idPaciente);
            if (factura == null) {
                txtResultado.setText("No hay conceptos pendientes para facturar.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Factura generada exitosamente\n");
                sb.append("Subtotal: $").append(factura.getSubtotal()).append("\n");
                sb.append("Impuesto: $").append(factura.getImpuesto()).append("\n");
                sb.append("Total: $").append(factura.getTotal()).append("\n");
                sb.append("Detalles:\n");
                factura.getDetalles().forEach(d -> sb.append(" - ").append(d.getConcepto()).append(": $").append(d.getMonto()).append("\n"));
                txtResultado.setText(sb.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
