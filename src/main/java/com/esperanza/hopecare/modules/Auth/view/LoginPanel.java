package com.esperanza.hopecare.modules.Auth.view;

import com.esperanza.hopecare.modules.Auth.dto.LoginDTO;
import com.esperanza.hopecare.modules.Auth.service.AuthService;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JLabel lblMensaje;
    private AuthService authService;
    private Runnable onLoginExitoso;

    public LoginPanel() {
        authService = new AuthService();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel("HopeCare - Iniciar Sesión", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        add(titulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        add(txtUsuario, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        txtContrasena = new JPasswordField(15);
        add(txtContrasena, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnLogin = new JButton("Ingresar");
        btnLogin.addActionListener(e -> handleLogin());
        add(btnLogin, gbc);

        gbc.gridy = 4;
        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setForeground(Color.RED);
        add(lblMensaje, gbc);

        gbc.gridy = 5;
        JButton btnRegistro = new JButton("¿No tienes cuenta? Regístrate");
        btnRegistro.addActionListener(e -> {
            if (onLoginExitoso != null) onLoginExitoso.run();
        });
        add(btnRegistro, gbc);
    }

    public void setOnLoginExitoso(Runnable r) {
        this.onLoginExitoso = r;
    }

    private void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        LoginDTO result = authService.login(usuario, contrasena);

        if (result.isExitoso()) {
            if (onLoginExitoso != null) onLoginExitoso.run();
        } else {
            lblMensaje.setText(result.getMensaje());
            txtContrasena.setText("");
        }
    }

    public void limpiarCampos() {
        txtUsuario.setText("");
        txtContrasena.setText("");
        lblMensaje.setText(" ");
    }
}
