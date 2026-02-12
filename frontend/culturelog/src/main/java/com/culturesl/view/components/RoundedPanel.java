package com.culturesl.view.components;

import com.culturesl.view.utils.ModernStyles;
import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int arc;

    public RoundedPanel(int arc) {
        this.arc = arc;
        setOpaque(false); // Importante para que se vean las curvas
        setBackground(ModernStyles.BG_PANEL);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}