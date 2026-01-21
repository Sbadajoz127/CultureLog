package com.culturesl.view.login;

import com.culturesl.api.AuthService;
import com.culturesl.dto.AuthResponse;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final AuthService authService;
    private JTextField userField;
    private JTextField emailField;
    private JPasswordField passField;
    private JButton registerButton;
    private JButton backButton;

    public RegisterFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Crear Cuenta - CultureLog");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        add(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // 1. TÍTULO
        JLabel titleLabel = new JLabel("Únete a CultureLog", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Crea tu cuenta gratis", SwingConstants.CENTER);
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(subtitleLabel, gbc);

        // 2. CAMPOS
        gbc.insets = new Insets(5, 0, 5, 0); // Reset insets

        // Usuario
        mainPanel.add(new JLabel("Nombre de Usuario:"), resetGBC(gbc, 2));
        userField = new JTextField();
        userField.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        userField.setPreferredSize(new Dimension(0, 35));
        mainPanel.add(userField, resetGBC(gbc, 3));

        // Email
        mainPanel.add(new JLabel("Correo Electrónico:"), resetGBC(gbc, 4));
        emailField = new JTextField();
        emailField.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        emailField.setPreferredSize(new Dimension(0, 35));
        mainPanel.add(emailField, resetGBC(gbc, 5));

        // Contraseña
        mainPanel.add(new JLabel("Contraseña:"), resetGBC(gbc, 6));
        passField = new JPasswordField();
        passField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; showRevealButton: true");
        passField.setPreferredSize(new Dimension(0, 35));
        mainPanel.add(passField, resetGBC(gbc, 7));

        // 3. BOTONES
        registerButton = new JButton("Registrarse");
        registerButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(new Color(46, 204, 113)); // Verde
        registerButton.setForeground(Color.WHITE);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridy = 8;
        gbc.insets = new Insets(25, 0, 10, 0);
        mainPanel.add(registerButton, gbc);

        // Botón para volver al login
        backButton = new JButton("¿Ya tienes cuenta? Inicia sesión");
        backButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false); // Transparente
        backButton.setForeground(new Color(68, 138, 255));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(backButton, gbc);

        // --- ACCIONES ---
        registerButton.addActionListener(e -> performRegister());
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private GridBagConstraints resetGBC(GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        return gbc;
    }

    private void performRegister() {
        String user = userField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());

        // Validaciones básicas visuales
        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Creando cuenta...");

        // Llamada Asíncrona al Backend
        new SwingWorker<AuthResponse, Void>() {
            @Override
            protected AuthResponse doInBackground() throws Exception {
                // LLAMADA REAL AL BACKEND
                return authService.register(user, pass, email);
            }

            @Override
            protected void done() {
                try {
                    AuthResponse response = get(); // Si llega aquí, todo fue bien (Status 201)
                    
                    JOptionPane.showMessageDialog(RegisterFrame.this, 
                        "¡Cuenta creada con éxito!\nAhora puedes iniciar sesión.", 
                        "Bienvenido", JOptionPane.INFORMATION_MESSAGE);

                    // Volver al Login automáticamente
                    new LoginFrame().setVisible(true);
                    dispose();

                } catch (Exception e) {
                    // Manejo de errores (Usuario duplicado, email existente, servidor caído)
                    // e.getCause().getMessage() suele traer el mensaje que enviamos desde el backend
                    String errorMsg = e.getMessage();
                    if (e.getCause() != null) errorMsg = e.getCause().getMessage();
                    
                    JOptionPane.showMessageDialog(RegisterFrame.this, 
                        "Error al registrar: " + errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                    
                    registerButton.setEnabled(true);
                    registerButton.setText("Registrarse");
                }
            }
        }.execute();
    }
}