package com.culturesl.view.login;

import com.culturesl.api.AuthService;
import com.culturesl.dto.AuthResponse;
import com.culturesl.model.UserSession;
import com.culturesl.view.main.MainFrame;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Login - CultureLog");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);

        // Panel principal con márgenes
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        add(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0); // Espacio vertical entre elementos
        gbc.gridx = 0;

        // 1. TÍTULO / LOGO
        JLabel titleLabel = new JLabel("CultureLog", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Tu universo multimedia", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0); // Más espacio debajo del subtítulo
        mainPanel.add(subtitleLabel, gbc);

        // 2. CAMPOS DE TEXTO
        gbc.insets = new Insets(5, 0, 5, 0); // Resetear insets

        mainPanel.add(new JLabel("Usuario:"), resetGBC(gbc, 2));
        userField = new JTextField();
        // Estilo redondeado de FlatLaf
        userField.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        userField.setPreferredSize(new Dimension(0, 35));
        mainPanel.add(userField, resetGBC(gbc, 3));

        mainPanel.add(new JLabel("Contraseña:"), resetGBC(gbc, 4));
        passField = new JPasswordField();
        passField.putClientProperty(FlatClientProperties.STYLE, "arc: 10; showRevealButton: true");
        passField.setPreferredSize(new Dimension(0, 35));
        mainPanel.add(passField, resetGBC(gbc, 5));

        JButton registerLink = new JButton("¿No tienes cuenta? Regístrate");
        registerLink.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false); // Fondo transparente
        registerLink.setForeground(new Color(68, 138, 255)); // Azul enlace
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridy = 7; // Una fila más abajo
        gbc.insets = new Insets(0, 0, 10, 0); // Pegado al botón de login
        mainPanel.add(registerLink, gbc);

        // Acción: Cerrar Login y abrir Registro
        registerLink.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        // 3. BOTÓN LOGIN
        loginButton = new JButton("Iniciar Sesión");
        loginButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(68, 138, 255)); // Azul
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridy = 6;
        gbc.insets = new Insets(30, 0, 10, 0);
        mainPanel.add(loginButton, gbc);

        // --- ACCIONES ---
        loginButton.addActionListener(e -> performLogin());
    }

    private GridBagConstraints resetGBC(GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        return gbc;
    }

    private void performLogin() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        loginButton.setEnabled(false); // Evitar doble clic
        loginButton.setText("Conectando...");

        // Ejecutar en hilo separado para no congelar la UI
        new SwingWorker<AuthResponse, Void>() {
            @Override
            protected AuthResponse doInBackground() throws Exception {
                return authService.login(user, pass);
            }

            @Override
            protected void done() {
                try {
                    AuthResponse response = get();
                    // GUARDAR SESIÓN
                    UserSession.getInstance().setUserId(response.getId());
                    UserSession.getInstance().setUsername(response.getUsername());
                    UserSession.getInstance().setEmail(response.getEmail());

                    // ABRIR DASHBOARD
                    new MainFrame().setVisible(true);
                    dispose(); // Cerrar login

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Error de acceso: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("Iniciar Sesión");
                }
            }
        }.execute();
    }
}