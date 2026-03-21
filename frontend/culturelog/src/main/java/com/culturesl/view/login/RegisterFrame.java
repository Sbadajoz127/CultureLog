package com.culturesl.view.login;

import com.culturesl.api.AuthService;
import com.culturesl.dto.AuthResponse;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class RegisterFrame {

    private Stage stage;
    private AuthService authService;

    private TextField userField;
    private TextField emailField;
    private PasswordField passField;
    private Button registerButton;

    public RegisterFrame(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        // Contenedor principal
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle("-fx-background-color: #121212;"); // Fondo oscuro

        // 1. TÍTULO
        Label titleLabel = new Label("Únete a CultureLog");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Crea tu cuenta gratis");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        VBox.setMargin(subtitleLabel, new Insets(0, 0, 15, 0));

        // 2. CAMPOS DE TEXTO
        String labelStyle = "-fx-text-fill: white;";
        String fieldStyle = "-fx-background-radius: 10; -fx-border-radius: 10; -fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #444;";

        // Usuario
        Label userLabel = new Label("Nombre de Usuario:");
        userLabel.setStyle(labelStyle);
        userField = new TextField();
        userField.setPrefHeight(35);
        userField.setStyle(fieldStyle);

        // Email
        Label emailLabel = new Label("Correo Electrónico:");
        emailLabel.setStyle(labelStyle);
        VBox.setMargin(emailLabel, new Insets(5, 0, 0, 0));
        emailField = new TextField();
        emailField.setPrefHeight(35);
        emailField.setStyle(fieldStyle);

        // Contraseña
        Label passLabel = new Label("Contraseña:");
        passLabel.setStyle(labelStyle);
        VBox.setMargin(passLabel, new Insets(5, 0, 0, 0));
        passField = new PasswordField();
        passField.setPrefHeight(35);
        passField.setStyle(fieldStyle);

        // Agrupar campos
        VBox formBox = new VBox(5, userLabel, userField, emailLabel, emailField, passLabel, passField);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setMaxWidth(300);

        // 3. BOTONES
        registerButton = new Button("Registrarse");
        registerButton.setPrefHeight(40);
        registerButton.setPrefWidth(Double.MAX_VALUE);
        // Verde (#2ecc71) como en tu código original
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        registerButton.setOnAction(e -> performRegister());

        Hyperlink backLink = new Hyperlink("¿Ya tienes cuenta? Inicia sesión");
        backLink.setStyle("-fx-text-fill: #448aff; -fx-border-color: transparent;");
        backLink.setOnAction(e -> {
            // Volver al Login
            new LoginFrame(stage);
        });

        VBox buttonBox = new VBox(15, registerButton, backLink);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(300);
        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));

        // Añadir todo al contenedor
        root.getChildren().addAll(titleLabel, subtitleLabel, formBox, buttonBox);

        // Mostrar la escena
        Scene scene = new Scene(root, 400, 550);
        stage.setTitle("Registro - CultureLog");
        stage.setScene(scene);
        stage.show();
    }

    private void performRegister() {
        String user = userField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passField.getText();

        // Validaciones básicas visuales
        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Todos los campos son obligatorios.");
            return;
        }

        registerButton.setDisable(true);
        registerButton.setText("Creando cuenta...");

        // Llamada Asíncrona al Backend
        CompletableFuture.supplyAsync(() -> {
            try {
                // LLAMADA REAL AL BACKEND
                return authService.register(user, pass, email);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).thenAccept(response -> {
            // Ejecutar UI updates en el hilo principal de JavaFX
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.INFORMATION, "Bienvenido", "¡Cuenta creada con éxito!\nAhora puedes iniciar sesión.");
                
                // Volver al Login automáticamente
                new LoginFrame(stage);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                showAlert(Alert.AlertType.ERROR, "Error de registro", "No se pudo registrar: " + errorMsg);
                
                registerButton.setDisable(false);
                registerButton.setText("Registrarse");
            });
            return null;
        });
    }

    // Método de utilidad para mostrar alertas en JavaFX
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}