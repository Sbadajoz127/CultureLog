package com.culturesl.view.main;

import com.culturesl.api.ApiClient;
import com.culturesl.dto.PostResponse;
import com.culturesl.view.components.RichPostCard;
import com.culturesl.view.components.RoundedPanel;
import com.culturesl.view.utils.ModernStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SocialHubPanel extends JPanel {

    public SocialHubPanel() {
        setLayout(new BorderLayout());
        setBackground(ModernStyles.BG_MAIN);
        setBorder(null);

        // A. SIDEBAR IZQUIERDA (Navegación)
        add(createLeftSidebar(), BorderLayout.WEST);

        // B. CENTRO (Feed + Tendencias)
        add(createCenterScrollable(), BorderLayout.CENTER);

        // C. SIDEBAR DERECHA (Recomendados)
        add(createRightSidebar(), BorderLayout.EAST);
    }

    // --- A. BARRA LATERAL IZQUIERDA ---
    private JPanel createLeftSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ModernStyles.BG_MAIN); // Fondo oscuro
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 0));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo
        JLabel logo = new JLabel("MediaHub");
        logo.setFont(ModernStyles.FONT_TITLE);
        logo.setForeground(Color.WHITE);
        logo.setIconTextGap(15);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        try {
            // Ajusta "logo.png" al nombre real de tu archivo
            java.net.URL imgUrl = getClass().getResource("/res/CultureLog_Blanco.png");            
            // Verificamos que cargó bien (si el ancho es -1, es que no la encontró)
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                
                // Redimensionar a 40x40 píxeles (o el tamaño que prefieras)
                Image scaledImg = originalIcon.getImage()
                                    .getScaledInstance(95, 60, Image.SCALE_SMOOTH);
                
                logo.setIcon(new ImageIcon(scaledImg));
                logo.setText(""); // Borramos el texto si la imagen cargó bien
            } else {
                System.err.println("⚠️ No se encontró la imagen: /res/CultureLog_Blanco.png");
                // Si sale esto, asegúrate de haber movido la carpeta a src/main/resources
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ---------------------------------

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(40));

        // Menú
        String[] menuItems = {"Películas", "Libros", "Música", "Fotografía", "Mi Colección", "Listas", "Comunidad"};

        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setForeground(ModernStyles.TEXT_SECONDARY);
            btn.setBackground(ModernStyles.BG_MAIN);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFont(ModernStyles.FONT_NORMAL);
            btn.setMaximumSize(new Dimension(200, 40));
            
            // Hover effect simple
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setForeground(Color.WHITE);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setForeground(ModernStyles.TEXT_SECONDARY);
                }
            });
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(5));
        }

        return sidebar;
    }

    // --- B. CENTRO (Feed y Tendencias) ---
    private JScrollPane createCenterScrollable() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ModernStyles.BG_MAIN);
        content.setBorder(new EmptyBorder(20, 20, 0, 20)); // Márgenes laterales

        // 1. Sección "Tendencias de Hoy" (Carrusel Horizontal simulado)
        JLabel lblTrends = new JLabel("Explora & Descubre");
        lblTrends.setFont(ModernStyles.FONT_TITLE);
        lblTrends.setForeground(Color.WHITE);
        lblTrends.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTrends);
        content.add(Box.createVerticalStrut(15));

        JPanel trendsPanel = new JPanel(new GridLayout(1, 3, 15, 0)); // 3 Columnas
        trendsPanel.setOpaque(false);
        trendsPanel.setMaximumSize(new Dimension(2000, 160)); // Altura fija
        trendsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir 3 tarjetas fake de tendencias
        trendsPanel.add(createTrendCard("Dune", "4.5", new Color(50, 40, 30)));
        trendsPanel.add(createTrendCard("El Gran Gatsby", "Book", new Color(30, 40, 50)));
        trendsPanel.add(createTrendCard("Pink Floyd", "Music", new Color(20, 20, 20)));

        content.add(trendsPanel);
        content.add(Box.createVerticalStrut(30));

        // 2. Feed de Posts
        JLabel lblFeed = new JLabel("Últimas Reseñas");
        lblFeed.setFont(ModernStyles.FONT_HEADER);
        lblFeed.setForeground(Color.WHITE);
        lblFeed.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblFeed);
        content.add(Box.createVerticalStrut(15));

        // Cargar posts reales
        loadPosts(content);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBorder(null);
        scroll.setBackground(ModernStyles.BG_MAIN);
        scroll.getViewport().setBackground(ModernStyles.BG_MAIN);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createTrendCard(String title, String subtitle, Color color) {
        RoundedPanel p = new RoundedPanel(15);
        p.setLayout(new BorderLayout());
        p.setBackground(color); // Color de fondo simulando la carátula
        
        JLabel lbl = new JLabel("<html><b>" + title + "</b><br><span style='font-size:9px'>" + subtitle + "</span></html>");
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    // --- C. SIDEBAR DERECHA (Recomendados) ---
    private JPanel createRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ModernStyles.BG_MAIN);
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(250, 0));

        // Barra de búsqueda simulada
        RoundedPanel searchBar = new RoundedPanel(20);
        searchBar.setBackground(ModernStyles.BG_PANEL);
        searchBar.setPreferredSize(new Dimension(200, 35));
        searchBar.setMaximumSize(new Dimension(2000, 35));
        searchBar.add(new JLabel("🔍 Buscar..."));
        sidebar.add(searchBar);
        sidebar.add(Box.createVerticalStrut(30));

        // Lista "Recomendado para Ti"
        JLabel lblRec = new JLabel("Recomendado para Ti");
        lblRec.setFont(ModernStyles.FONT_HEADER);
        lblRec.setForeground(Color.WHITE);
        sidebar.add(lblRec);
        sidebar.add(Box.createVerticalStrut(15));

        // Items pequeños
        sidebar.add(createMiniItem("Los Siete Samuráis", "Película"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMiniItem("1984", "George Orwell"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMiniItem("Dark Side of the Moon", "Pink Floyd"));

        return sidebar;
    }

    private JPanel createMiniItem(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(2000, 50));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel icon = new RoundedPanel(10);
        icon.setPreferredSize(new Dimension(40, 40));
        icon.setBackground(Color.DARK_GRAY);
        
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        JLabel l1 = new JLabel(title);
        l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel(subtitle);
        l2.setForeground(Color.GRAY);
        l2.setFont(ModernStyles.FONT_SMALL);
        texts.add(l1);
        texts.add(l2);

        p.add(icon, BorderLayout.WEST);
        p.add(texts, BorderLayout.CENTER);
        return p;
    }

    // Lógica de carga (Igual que antes, pero usando RichPostCard)
    private void loadPosts(JPanel container) {
        SwingWorker<List<PostResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PostResponse> doInBackground() throws Exception {
                // AQUÍ USAS TU API REAL.
                // Como ejemplo devuelvo lista vacía o tu mock
                return ApiClient.getInstance().getFeed(0);
            }

            @Override
            protected void done() {
                try {
                    List<PostResponse> posts = get();
                    for (PostResponse p : posts) {
                        RichPostCard card = new RichPostCard(p);
                        card.setAlignmentX(Component.LEFT_ALIGNMENT);
                        container.add(card);
                        container.add(Box.createVerticalStrut(20)); // Espacio entre posts
                    }
                    container.revalidate();
                    container.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }
}