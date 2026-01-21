package com.culturesl.view.main;

import com.culturesl.model.UserSession;
import com.culturesl.view.login.LoginFrame;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("CultureLog - " + UserSession.getInstance().getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // 1. TOOLBAR SUPERIOR
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(Box.createHorizontalStrut(10));
        
        JButton btnFeed = createNavButton("Feed Social", "feed");
        JButton btnLibrary = createNavButton("Mi Biblioteca", "library");
        JButton btnProfile = createNavButton("Perfil", "profile");
        
        toolbar.add(btnFeed);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnLibrary);
        toolbar.add(Box.createHorizontalGlue()); // Empuja lo siguiente a la derecha
        
        // Botón Perfil / Logout a la derecha
        toolbar.add(new JLabel("Hola, " + UserSession.getInstance().getUsername() + "  "));
        toolbar.add(btnProfile);
        
        JButton btnLogout = new JButton("Salir");
        btnLogout.addActionListener(e -> logout());
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnLogout);
        toolbar.add(Box.createHorizontalStrut(10));

        add(toolbar, BorderLayout.NORTH);

        // 2. PANEL CENTRAL (CONTENIDO CAMBIANTE)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Aquí añadiremos los paneles reales más adelante
        contentPanel.add(new JLabel("AQUÍ IRÁ EL FEED SOCIAL", SwingConstants.CENTER), "feed");
        contentPanel.add(new JLabel("AQUÍ IRÁ LA BIBLIOTECA", SwingConstants.CENTER), "library");
        contentPanel.add(new JLabel("AQUÍ IRÁ EL PERFIL", SwingConstants.CENTER), "profile");

        add(contentPanel, BorderLayout.CENTER);
        
        // Acciones de navegación
        btnFeed.addActionListener(e -> cardLayout.show(contentPanel, "feed"));
        btnLibrary.addActionListener(e -> cardLayout.show(contentPanel, "library"));
        btnProfile.addActionListener(e -> cardLayout.show(contentPanel, "profile"));
    }

    private JButton createNavButton(String text, String actionCommand) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        return btn;
    }

    private void logout() {
        UserSession.getInstance().cleanSession();
        new LoginFrame().setVisible(true);
        dispose();
    }
}