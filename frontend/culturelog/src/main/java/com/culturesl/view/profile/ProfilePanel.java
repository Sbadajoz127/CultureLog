package com.culturesl.view.profile;

import com.culturesl.model.UserSession;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.CompletableFuture;

// En JavaFX, los paneles heredan de contenedores como VBox, HBox, StackPane, etc.
public class ProfilePanel extends VBox {

    private Stage stage;

    // Componentes UI
    private Circle avatarCircle;
    private TextField txtUsername;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private ComboBox<String> cmbTheme;
    private Button btnSave;

    private File selectedPhotoFile;

    public ProfilePanel(Stage stage) {
        this.stage = stage;
        initComponents();
        loadUserData();
    }

    private void initComponents() {
        // Layout Principal: VBox centrado (ocupa el espacio de this)
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #121212;"); // Fondo oscuro

        // Contenedor del formulario interno para restringir el ancho
        VBox contentPanel = new VBox(10); // Espaciado vertical de 10px
        contentPanel.setAlignment(Pos.CENTER);
        contentPanel.setMaxWidth(300); // Para que los inputs no se estiren infinitamente

        // ==========================================
        // 1. FOTO DE PERFIL (Avatar Circular)
        // ==========================================
        avatarCircle = new Circle(65); // Radio de 65 = diámetro de 130
        avatarCircle.setStroke(Color.web("#333333"));
        avatarCircle.setStrokeWidth(2);
        setAvatarImage(null); // Pone la inicial por defecto

        Button btnUpload = new Button("Cambiar Foto");
        btnUpload.setStyle("-fx-background-color: transparent; -fx-text-fill: #448aff; -fx-cursor: hand;");
        btnUpload.setOnAction(e -> selectPhoto());

        VBox.setMargin(btnUpload, new Insets(5, 0, 20, 0)); // Margen inferior

        contentPanel.getChildren().addAll(avatarCircle, btnUpload);

        // ==========================================
        // 2. CAMPOS
        // ==========================================
        txtUsername = createCenteredTextField("Nombre de Usuario", contentPanel);
        txtEmail = createCenteredTextField("Correo Electrónico", contentPanel);
        txtPassword = createCenteredPasswordField("Nueva Contraseña", contentPanel);

        // --- Tema ---
        Label lblTheme = new Label("Tema Visual");
        lblTheme.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
        
        cmbTheme = new ComboBox<>();
        cmbTheme.getItems().addAll("Oscuro (Dark)", "Claro (Light)");
        cmbTheme.setValue("Oscuro (Dark)"); // Valor por defecto
        cmbTheme.setPrefWidth(Double.MAX_VALUE); // Para que ocupe todo el ancho disponible del VBox
        cmbTheme.setStyle("-fx-background-radius: 5; -fx-background-color: #2a2a2a; -fx-text-fill: white;");
        // Ojo: Dar estilo a la lista desplegable de un ComboBox en JavaFX es complejo mediante CSS en línea.
        // Si no se ve perfecto el menú, es normal, se suele usar un archivo .css externo para afinarlo.

        VBox themeBox = new VBox(5, lblTheme, cmbTheme);
        themeBox.setAlignment(Pos.CENTER);
        VBox.setMargin(themeBox, new Insets(0, 0, 20, 0));
        contentPanel.getChildren().add(themeBox);

        // ==========================================
        // 3. BOTÓN GUARDAR
        // ==========================================
        btnSave = new Button("Guardar Cambios");
        btnSave.setPrefHeight(40);
        btnSave.setPrefWidth(200);
        btnSave.setStyle("-fx-background-color: #448aff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnSave.setOnAction(e -> saveChanges());

        contentPanel.getChildren().add(btnSave);

        // Añadir el formulario centrado al panel principal (this)
        this.getChildren().add(contentPanel);
    }

    // --- HELPERS PARA CAMPOS DE TEXTO ---
    private TextField createCenteredTextField(String labelText, VBox parent) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");

        TextField field = new TextField();
        field.setPrefHeight(35);
        field.setAlignment(Pos.CENTER); // Centra el texto escrito
        field.setStyle("-fx-background-radius: 5; -fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #444; -fx-border-radius: 5;");

        VBox box = new VBox(5, label, field);
        box.setAlignment(Pos.CENTER);
        VBox.setMargin(box, new Insets(0, 0, 15, 0)); // Margen inferior
        parent.getChildren().add(box);

        return field;
    }

    private PasswordField createCenteredPasswordField(String labelText, VBox parent) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");

        PasswordField field = new PasswordField();
        field.setPrefHeight(35);
        field.setAlignment(Pos.CENTER);
        field.setStyle("-fx-background-radius: 5; -fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #444; -fx-border-radius: 5;");

        VBox box = new VBox(5, label, field);
        box.setAlignment(Pos.CENTER);
        VBox.setMargin(box, new Insets(0, 0, 15, 0));
        parent.getChildren().add(box);

        return field;
    }

    // --- UTILIDADES ---
    private void setAvatarImage(File file) {
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            avatarCircle.setFill(new ImagePattern(image));
        } else {
            // Si no hay foto, ponemos un color de fondo plano.
            // En JavaFX es más complejo dibujar texto dentro del círculo que en Swing.
            // Lo más sencillo es dejar el fondo de un color.
            avatarCircle.setFill(Color.web("#555555"));
            // (Nota: Si quieres la letra inicial obligatoriamente, habría que usar un StackPane
            // combinando el Círculo de fondo y un Text/Label encima).
        }
    }

    private void selectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto de perfil");
        // Filtros de extensión
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedPhotoFile = file;
            setAvatarImage(file);
        }
    }

    private void loadUserData() {
        if (UserSession.getInstance() != null && UserSession.getInstance().getUsername() != null) {
            txtUsername.setText(UserSession.getInstance().getUsername());
            txtEmail.setText(UserSession.getInstance().getEmail() != null ? UserSession.getInstance().getEmail() : "");
        }
    }

    private void saveChanges() {
        btnSave.setDisable(true);
        btnSave.setText("Guardando...");

        // Simulamos el guardado asíncrono
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // Simula el delay de red
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }).thenAccept(result -> {
            Platform.runLater(() -> {
                btnSave.setDisable(false);
                btnSave.setText("Guardar Cambios");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText(null);
                alert.setContentText("Perfil actualizado correctamente.");
                alert.showAndWait();
            });
        });
    }
}