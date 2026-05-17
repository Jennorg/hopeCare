package com.esperanza.hopecare.modules.Auth.view;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.Auth.dto.RegistroDTO;
import com.esperanza.hopecare.modules.Auth.service.AuthService;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SignUpPanel extends JPanel {
    private JTextField txtUsuario, txtNombre, txtApellido, txtDocumento, txtTelefono, txtEmail;
    private JPasswordField txtContrasena, txtConfirmar;
    private JComboBox<String> cmbRol, cmbEspecialidad;
    private JLabel lblMensaje;
    private AuthService authService;
    private Runnable onRegistroExitoso;

    public SignUpPanel() {
        authService = new AuthService();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        JLabel titulo = new JLabel("HopeCare - Crear Cuenta", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(titulo, gbc);
        gbc.gridwidth = 1;

        y = addField(gbc, y, "Usuario:", txtUsuario = new JTextField(15));
        y = addField(gbc, y, "Contraseña:", txtContrasena = new JPasswordField(15));
        y = addField(gbc, y, "Confirmar:", txtConfirmar = new JPasswordField(15));

        gbc.gridx = 0; gbc.gridy = y++;
        add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1;
        cmbRol = new JComboBox<>(new String[]{"ADMINISTRADOR", "PACIENTE", "MEDICO"});
        add(cmbRol, gbc);

        y = addField(gbc, y, "Nombre:", txtNombre = new JTextField(15));
        y = addField(gbc, y, "Apellido:", txtApellido = new JTextField(15));
        y = addField(gbc, y, "Cédula:", txtDocumento = new JTextField(15));
        y = addField(gbc, y, "Teléfono:", txtTelefono = new JTextField(15));
        y = addField(gbc, y, "Email:", txtEmail = new JTextField(15));

        gbc.gridx = 0; gbc.gridy = y++;
        add(new JLabel("Especialidad:"), gbc);
        gbc.gridx = 1;
        cmbEspecialidad = new JComboBox<>(cargarEspecialidades().toArray(new String[0]));
        add(cmbEspecialidad, gbc);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        JButton btnRegistrar = new JButton("Crear Cuenta");
        btnRegistrar.addActionListener(e -> handleRegistro());
        add(btnRegistrar, gbc);

        gbc.gridy = y++;
        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setForeground(Color.RED);
        add(lblMensaje, gbc);

        gbc.gridy = y;
        JButton btnLogin = new JButton("Volver al Login");
        btnLogin.addActionListener(e -> {
            if (onRegistroExitoso != null) onRegistroExitoso.run();
        });
        add(btnLogin, gbc);
    }

    public void setOnRegistroExitoso(Runnable r) {
        this.onRegistroExitoso = r;
    }

    private int addField(GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel(label), gbc);
        gbc.gridx = 1;
        add(field, gbc);
        return y + 1;
    }

    private void handleRegistro() {
        String usuario = txtUsuario.getText().trim();
        String pass = new String(txtContrasena.getPassword());
        String confirm = new String(txtConfirmar.getPassword());
        String telefono = txtTelefono.getText().trim();
        String documento = txtDocumento.getText().trim();
        String email = txtEmail.getText().trim();

        if (usuario.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblMensaje.setText("Todos los campos son obligatorios.");
            return;
        }
        if (!pass.equals(confirm)) {
            lblMensaje.setText("Las contraseñas no coinciden.");
            return;
        }
        if (!documento.isEmpty() && !documento.matches("\\d+")) {
            lblMensaje.setText("La cédula debe contener solo números.");
            return;
        }
        if (!documento.isEmpty() && (documento.length() < 5 || documento.length() > 20)) {
            lblMensaje.setText("La cédula debe tener entre 5 y 20 dígitos.");
            return;
        }
        if (!telefono.isEmpty() && !telefono.matches("\\d+")) {
            lblMensaje.setText("El teléfono debe contener solo números.");
            return;
        }
        if (!telefono.isEmpty() && (telefono.length() < 7 || telefono.length() > 15)) {
            lblMensaje.setText("El teléfono debe tener entre 7 y 15 dígitos.");
            return;
        }

        RegistroDTO dto = new RegistroDTO();
        dto.setNombreUsuario(usuario);
        dto.setContrasena(pass);
        dto.setRol((String) cmbRol.getSelectedItem());
        dto.setNombre(txtNombre.getText().trim());
        dto.setApellido(txtApellido.getText().trim());
        dto.setDocumento(documento);
        dto.setTelefono(telefono);
        dto.setEmail(email);
        dto.setEspecialidad((String) cmbEspecialidad.getSelectedItem());

        String error = authService.registrar(dto);

        if (error != null) {
            lblMensaje.setText(error);
        } else {
            JOptionPane.showMessageDialog(this,
                "Cuenta registrada exitosamente.\nYa puedes iniciar sesión.",
                "Registro completado", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            if (onRegistroExitoso != null) onRegistroExitoso.run();
        }
    }

    private List<String> cargarEspecialidades() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_especialidad FROM especialidad ORDER BY nombre_especialidad";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("nombre_especialidad"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lista.isEmpty()) lista.add("Otra especialidad");
        return lista;
    }

    public void limpiarCampos() {
        txtUsuario.setText("");
        txtContrasena.setText("");
        txtConfirmar.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDocumento.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        lblMensaje.setText(" ");
    }
}
