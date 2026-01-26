package com.culturesl;

import com.culturesl.view.login.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Instalar Tema Oscuro
                FlatDarkLaf.setup();
                
                // Opcional: Personalizar color de acento
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);

                new LoginFrame().setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}