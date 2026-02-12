package com.culturesl.view.profile;

import com.culturesl.api.ApiClient; // Tu cliente API
import com.culturesl.model.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfilePanel extends JPanel {

    private JTextField txtUsername;
    private JTextField txtEmail;
    private JButton btnSave;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 50, 30, 50)); // Márgenes externos

        // Título
        JLabel lblTitle = new JLabel("Editar Mi Perfil");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Formulario Central
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Espacio entre elementos
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Campo Usuario ---
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nombre de Usuario:"), gbc);

        gbc.gridx = 1; 
        gbc.weightx = 1.0; // Estirar a lo ancho
        txtUsername = new JTextField(20);
        formPanel.add(txtUsername, gbc);

        // --- Campo Email ---
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Correo Electrónico:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        // --- Botón Guardar ---
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE; // No estirar botón
        gbc.anchor = GridBagConstraints.EAST; // Pegar a la derecha
        
        btnSave = new JButton("Guardar Cambios");
        btnSave.setBackground(new Color(68, 138, 255));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        btnSave.addActionListener(e -> saveChanges());
        
        formPanel.add(btnSave, gbc);

        // Añadir formulario al panel principal
        // Lo ponemos en un panel wrapper para que se quede arriba y no se estire feo
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(formPanel, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);

        loadUserData();
    }

    private void loadUserData() {
        // Cargar datos de la sesión actual
        txtUsername.setText(UserSession.getInstance().getUsername());
        // El email deberías traerlo de UserSession o hacer una llamada a la API aquí
        // txtEmail.setText(UserSession.getInstance().getEmail()); 
    }

    private void saveChanges() {
        String newUsername = txtUsername.getText().trim();
        String newEmail = txtEmail.getText().trim();

        if(newUsername.isEmpty() || newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");

        // Llamada a la API en segundo plano
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // AQUÍ LLAMAS A TU BACKEND (Necesitas crear este método en ApiClient)
                // return ApiClient.getInstance().updateUser(newUsername, newEmail);
                
                Thread.sleep(1000); // Simulamos retardo de red
                return true; // Simulamos éxito
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ProfilePanel.this, "Perfil actualizado con éxito.");
                        // Actualizar sesión local si es necesario
                        // UserSession.getInstance().setUsername(newUsername);
                    } else {
                        JOptionPane.showMessageDialog(ProfilePanel.this, "Error al actualizar.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    btnSave.setEnabled(true);
                    btnSave.setText("Guardar Cambios");
                }
            }
        };
        worker.execute();
    }
}
