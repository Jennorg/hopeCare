package com.esperanza.hopecare.modules.citas_consultas.view;

import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.presenter.CitaPresenter;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CitasPanel extends JPanel implements ICitaView {
    private JTextField txtIdPaciente, txtIdMedico;
    private JFormattedTextField txtFecha;
    private JComboBox<String> cbHorarios;
    private JButton btnBuscar, btnReservar;
    private CitaPresenter presenter;
    private List<LocalTime> horariosActuales;
    
    public CitasPanel() {
        presenter = new CitaPresenter(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        
        gbc.gridx=0; gbc.gridy=0; add(new JLabel("ID Paciente:"), gbc);
        txtIdPaciente = new JTextField(10);
        gbc.gridx=1; add(txtIdPaciente, gbc);
        
        gbc.gridx=0; gbc.gridy=1; add(new JLabel("ID Médico:"), gbc);
        txtIdMedico = new JTextField(10);
        gbc.gridx=1; add(txtIdMedico, gbc);
        
        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);
        txtFecha = new JFormattedTextField(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        txtFecha.setColumns(10);
        gbc.gridx=1; add(txtFecha, gbc);
        
        btnBuscar = new JButton("Buscar horarios");
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        add(btnBuscar, gbc);
        
        gbc.gridy=4; add(new JLabel("Horarios disponibles:"), gbc);
        cbHorarios = new JComboBox<>();
        cbHorarios.setEnabled(false);
        gbc.gridx=1; add(cbHorarios, gbc);
        
        btnReservar = new JButton("Reservar cita");
        gbc.gridy=5; gbc.gridx=0; gbc.gridwidth=2;
        btnReservar.setEnabled(false);
        add(btnReservar, gbc);
        
        btnBuscar.addActionListener(e -> buscarHorarios());
        btnReservar.addActionListener(e -> presenter.reservarCita());
    }
    
    private void buscarHorarios() {
        try {
            int idMedico = Integer.parseInt(txtIdMedico.getText());
            LocalDate fecha = LocalDate.parse(txtFecha.getText());
            presenter.actualizarHorariosDisponibles(idMedico, fecha);
        } catch (Exception ex) {
            mostrarMensajeError("Datos inválidos: " + ex.getMessage());
        }
    }
    
    @Override
    public void mostrarCitasExistentes(List<Cita> citas) {}

    @Override
    public void mostrarHorariosDisponibles(List<LocalTime> bloques) {
        horariosActuales = bloques;
        cbHorarios.removeAllItems();
        if (bloques.isEmpty()) {
            cbHorarios.addItem("No hay horarios");
            cbHorarios.setEnabled(false);
            btnReservar.setEnabled(false);
        } else {
            for (LocalTime t : bloques) {
                cbHorarios.addItem(t.toString());
            }
            cbHorarios.setEnabled(true);
            btnReservar.setEnabled(true);
        }
    }
    
    @Override
    public void mostrarMensajeError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    @Override
    public void mostrarMensajeExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        limpiarCampos();
    }
    
    @Override
    public void limpiarCampos() {
        txtIdPaciente.setText("");
        txtIdMedico.setText("");
        txtFecha.setText("");
        cbHorarios.removeAllItems();
        cbHorarios.setEnabled(false);
        btnReservar.setEnabled(false);
    }
    
    @Override
    public int getIdPacienteSeleccionado() {
        return Integer.parseInt(txtIdPaciente.getText());
    }
    
    @Override
    public int getIdMedicoSeleccionado() {
        return Integer.parseInt(txtIdMedico.getText());
    }
    
    @Override
    public LocalDate getFechaSeleccionada() {
        return LocalDate.parse(txtFecha.getText());
    }
    
    @Override
    public void mostrarDiasDisponibles(List<Integer> diasSemana) {}

    @Override
    public int getDiaSeleccionado() {
        return -1;
    }

    @Override
    public double getPrecio() { return 0.0; }

    @Override
    public LocalTime getHoraSeleccionada() {
        String selected = (String) cbHorarios.getSelectedItem();
        return LocalTime.parse(selected);
    }
}
