package com.culturesl.view.profile;

import com.culturesl.model.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ProfilePanel extends JPanel {

    // Componentes UI
    private JLabel lblAvatar;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbTheme;
    private JButton btnSave;
    
    private File selectedPhotoFile;

    public ProfilePanel() {
        // 1. LAYOUT PRINCIPAL: GridBagLayout para centrar el panel de contenido en la ventana
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 2. PANEL DE CONTENIDO (PILA VERTICAL)
        // Usamos BoxLayout vertical o GridBagLayout con 1 sola columna
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Asegura que el panel se centre

        // ==========================================
        // 1. FOTO DE PERFIL
        // ==========================================
        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(130, 130));
        lblAvatar.setMaximumSize(new Dimension(130, 130)); // Evita que se estire
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar componente
        lblAvatar.setIcon(createCircleIcon(null, 130));
        
        contentPanel.add(lblAvatar);
        contentPanel.add(Box.createVerticalStrut(10)); // Espacio

        JButton btnUpload = new JButton("Cambiar Foto");
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUpload.setFocusPainted(false);
        btnUpload.addActionListener(e -> selectPhoto());
        
        contentPanel.add(btnUpload);
        contentPanel.add(Box.createVerticalStrut(30)); // Separación grande

        // ==========================================
        // 2. CAMPOS (Etiqueta Arriba, Input Abajo, Todo Centrado)
        // ==========================================
        
        // --- Username ---
        addCenteredField(contentPanel, "Nombre de Usuario", txtUsername = new JTextField(20));
        
        // --- Email ---
        addCenteredField(contentPanel, "Correo Electrónico", txtEmail = new JTextField(20));

        // --- Password ---
        addCenteredField(contentPanel, "Nueva Contraseña", txtPassword = new JPasswordField(20));

        // --- Tema ---
        // El ComboBox requiere un trato especial para que no se estire feo
        JLabel lblTheme = new JLabel("Tema Visual");
        lblTheme.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTheme.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTheme.setForeground(Color.GRAY);
        contentPanel.add(lblTheme);
        contentPanel.add(Box.createVerticalStrut(5));

        cmbTheme = new JComboBox<>(new String[]{"Oscuro (Dark)", "Claro (Light)"});
        cmbTheme.setMaximumSize(new Dimension(225, 30)); // Ancho fijo igual que los textfields
        cmbTheme.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(cmbTheme);
        contentPanel.add(Box.createVerticalStrut(20));

        // ==========================================
        // 3. BOTÓN GUARDAR
        // ==========================================
        contentPanel.add(Box.createVerticalStrut(10));
        
        btnSave = new JButton("Guardar Cambios");
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSave.setBackground(new Color(68, 138, 255));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setMaximumSize(new Dimension(200, 40)); // Tamaño fijo
        btnSave.setPreferredSize(new Dimension(200, 40));
        
        btnSave.addActionListener(e -> saveChanges());
        
        contentPanel.add(btnSave);

        // AÑADIR AL PANEL PRINCIPAL
        add(contentPanel);

        loadUserData();
    }

    // --- HELPER PARA AÑADIR CAMPOS CENTRADOS ---
    private void addCenteredField(JPanel panel, String labelText, JTextField field) {
        // 1. Etiqueta
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.GRAY); // Color suave para la etiqueta
        
        // 2. Campo
        field.setMaximumSize(new Dimension(225, 30)); // Ancho fijo para que todos sean iguales
        field.setHorizontalAlignment(JTextField.CENTER); // ¡TEXTO CENTRADO DENTRO DEL CAMPO!
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(5)); // Espacio pequeño entre label y campo
        panel.add(field);
        panel.add(Box.createVerticalStrut(15)); // Espacio entre grupos
    }

    // --- UTILIDADES ---
    private Icon createCircleIcon(Image img, int size) {
        BufferedImage avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = avatar.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        
        if (img != null) {
            g2.drawImage(img, 0, 0, size, size, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillRect(0, 0, size, size);
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, size / 2));
            String initial = UserSession.getInstance() != null ? 
                             UserSession.getInstance().getUsername().substring(0, 1).toUpperCase() : "?";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, (size - fm.stringWidth(initial)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
        return new ImageIcon(avatar);
    }

    private void selectPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                selectedPhotoFile = fileChooser.getSelectedFile();
                BufferedImage img = ImageIO.read(selectedPhotoFile);
                lblAvatar.setIcon(createCircleIcon(img, 130));
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void loadUserData() {
        if(UserSession.getInstance() != null) {
            txtUsername.setText(UserSession.getInstance().getUsername());
        }
    }

    private void saveChanges() {
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Thread.sleep(1000); 
                return true;
            }
            @Override
            protected void done() {
                btnSave.setEnabled(true);
                btnSave.setText("Guardar Cambios");
                JOptionPane.showMessageDialog(ProfilePanel.this, "Perfil actualizado.");
            }
        };
        worker.execute();
    }
}