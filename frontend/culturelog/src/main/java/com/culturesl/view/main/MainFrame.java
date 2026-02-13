package com.culturesl.view.main;

import com.culturesl.model.UserSession;
import com.culturesl.view.login.LoginFrame;
import com.culturesl.view.profile.ProfilePanel;
import com.culturesl.view.utils.ModernStyles;

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
        getContentPane().setBackground(ModernStyles.BG_MAIN);

        // 1. TOOLBAR SUPERIOR
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(Box.createHorizontalStrut(10));
        
        JButton btnFeed = createNavButton("Feed Social", "feed");
        JButton btnLibrary = createNavButton("Mi Biblioteca", "library");
        JButton btnMisPublicaciones = createNavButton("Mis Publicaciones", "misPublicaciones");
        JButton btnProfile = createNavButton("Perfil", "profile");
        
        toolbar.add(btnFeed);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnLibrary);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnMisPublicaciones);
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
        contentPanel.setBorder(null);
        contentPanel.setBackground(new Color(18,18,18)); // Fondo blanco para el contenido

        // Aquí añadiremos los paneles reales más adelante
        contentPanel.add(new SocialHubPanel(), "feed");
        contentPanel.add(new ProfilePanel(), "profile");

        contentPanel.add(new JLabel("AQUÍ IRÁ LA BIBLIOTECA", SwingConstants.CENTER), "library");
        contentPanel.add(new JLabel("AQUÍ IRÁ MIS PUBLICACIONES", SwingConstants.CENTER), "misPublicaciones");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Acciones de navegación
        btnFeed.addActionListener(e -> cardLayout.show(contentPanel, "feed"));
        btnMisPublicaciones.addActionListener(e -> cardLayout.show(contentPanel, "misPublicaciones"));
        btnLibrary.addActionListener(e -> cardLayout.show(contentPanel, "library"));
        btnProfile.addActionListener(e -> cardLayout.show(contentPanel, "profile"));

        cardLayout.show(contentPanel, "feed"); // Mostrar el feed por defecto
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
