package com.culturesl.view.components;

import com.culturesl.dto.PostResponse; // Ajusta el paquete según tu proyecto
import com.culturesl.view.utils.ModernStyles;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class RichPostCard extends RoundedPanel {

    public RichPostCard(PostResponse post) {
        super(20); // Bordes redondeados de 20px
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(ModernStyles.BG_PANEL);

        // --- 1. CABECERA (Avatar + Nombre) ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        
        // Avatar simulado (Círculo)
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.fill(new Ellipse2D.Float(0, 0, 40, 40));
                // Aquí podrías pintar la imagen real
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        JLabel lblName = new JLabel(post.getAuthorName());
        lblName.setFont(ModernStyles.FONT_HEADER);
        lblName.setForeground(ModernStyles.TEXT_PRIMARY);
        
        JLabel lblAction = new JLabel("ha publicado una reseña");
        lblAction.setFont(ModernStyles.FONT_SMALL);
        lblAction.setForeground(ModernStyles.TEXT_SECONDARY);
        
        texts.add(lblName);
        texts.add(lblAction);
        
        header.add(avatar);
        header.add(texts);
        add(header, BorderLayout.NORTH);

        // --- 2. CONTENIDO VISUAL (La "Portada" estilo Banner) ---
        JPanel centerContent = new JPanel(new BorderLayout(0, 10));
        centerContent.setOpaque(false);
        
        JLabel txtContent = new JLabel("<html><body style='width: 300px'>" + post.getContent() + "</body></html>");
        txtContent.setFont(ModernStyles.FONT_NORMAL);
        txtContent.setForeground(ModernStyles.TEXT_PRIMARY);
        centerContent.add(txtContent, BorderLayout.NORTH);

        // Si hay película/libro vinculado, dibujamos el banner GRANDE
        if (post.getLinkedItemTitle() != null) {
            JPanel banner = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Simular imagen de fondo (Dune, Blade Runner...)
                    Graphics2D g2 = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(100, 50, 0), 0, getHeight(), new Color(20, 10, 0));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                }
            };
            banner.setPreferredSize(new Dimension(0, 180)); // Altura fija como en la foto
            banner.setBorder(new EmptyBorder(15, 15, 15, 15));
            banner.setOpaque(false);
            
            JLabel lblTitle = new JLabel(post.getLinkedItemTitle().toUpperCase());
            lblTitle.setFont(ModernStyles.FONT_TITLE);
            lblTitle.setForeground(Color.WHITE);
            
            JLabel lblRating = new JLabel("★ " + (post.getLinkedItemRating() != null ? post.getLinkedItemRating() : "?") + "/5");
            lblRating.setForeground(new Color(255, 193, 7)); // Dorado
            
            banner.add(lblTitle, BorderLayout.CENTER);
            banner.add(lblRating, BorderLayout.SOUTH);
            
            centerContent.add(banner, BorderLayout.CENTER);
        }
        
        add(centerContent, BorderLayout.CENTER);

        // --- 3. FOOTER (Likes y Comentarios) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        footer.setOpaque(false);
        
        footer.add(createStatIcon("♥ " + post.getLikeCount()));
        footer.add(createStatIcon("💬 " + post.getCommentCount() + " comentarios"));
        
        add(footer, BorderLayout.SOUTH);
    }
    
    private JLabel createStatIcon(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(ModernStyles.TEXT_SECONDARY);
        lbl.setFont(ModernStyles.FONT_SMALL);
        return lbl;
    }
}