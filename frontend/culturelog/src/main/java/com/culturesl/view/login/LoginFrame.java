package com.culturesl.view.login;

import com.culturesl.api.AuthService;
import com.culturesl.dto.AuthResponse;
import com.culturesl.model.UserSession;
import com.culturesl.view.main.MainFrame;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class LoginFrame {

    private Stage stage;
    private AuthService authService;

    private TextField userField;
    private PasswordField passField;
    private Button loginButton;

    // Recibimos el Stage (la ventana principal) por constructor
    public LoginFrame(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        // Contenedor principal (Vertical Box)
        VBox root = new VBox(15); 
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #121212;"); // Fondo oscuro moderno

        // 1. TÍTULO / LOGO
        Label titleLabel = new Label("CultureLog");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Tu universo multimedia");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: gray;");
        VBox.setMargin(subtitleLabel, new Insets(0, 0, 20, 0));

        // 2. CAMPOS DE TEXTO
        Label userLabel = new Label("Usuario:");
        userLabel.setStyle("-fx-text-fill: white;");
        
        userField = new TextField();
        userField.setPrefHeight(35);
        userField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #444;");

        Label passLabel = new Label("Contraseña:");
        passLabel.setStyle("-fx-text-fill: white;");
        VBox.setMargin(passLabel, new Insets(10, 0, 0, 0)); // Un poco de espacio extra arriba
        
        passField = new PasswordField();
        passField.setPrefHeight(35);
        passField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-border-color: #444;");

        // Agrupamos los campos en su propia cajita para que no se estiren al 100% de la pantalla
        VBox formBox = new VBox(5, userLabel, userField, passLabel, passField);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setMaxWidth(300);

        // 3. ENLACE Y BOTÓN
        Hyperlink registerLink = new Hyperlink("¿No tienes cuenta? Regístrate");
        registerLink.setStyle("-fx-text-fill: #448aff; -fx-border-color: transparent;");
        registerLink.setOnAction(e -> {
            // Aquí llamarás a tu RegisterFrame de JavaFX cuando lo crees
            // new RegisterFrame(stage); 
            System.out.println("Abriendo ventana de registro...");
        });

        loginButton = new Button("Iniciar Sesión");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(Double.MAX_VALUE); // Para que ocupe el ancho del formulario
        loginButton.setStyle("-fx-background-color: #448aff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        loginButton.setOnAction(e -> performLogin());

        // Agrupamos el botón y el enlace
        VBox buttonBox = new VBox(15, loginButton, registerLink);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(300);
        VBox.setMargin(buttonBox, new Insets(30, 0, 0, 0));

        // Añadimos todo al contenedor principal
        root.getChildren().addAll(titleLabel, subtitleLabel, formBox, buttonBox);

        // Configuramos la escena y la mostramos en el escenario (Stage)
        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("Login - CultureLog");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void performLogin() {
        String user = userField.getText();
        String pass = passField.getText();

        loginButton.setDisable(true);
        loginButton.setText("Conectando...");

        // Ejecutamos la petición en un hilo secundario
        CompletableFuture.supplyAsync(() -> {
            try {
                return authService.login(user, pass);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).thenAccept(response -> {
            // Platform.runLater obliga a JavaFX a actualizar la interfaz gráfica de forma segura
            Platform.runLater(() -> {
                // GUARDAR SESIÓN
                UserSession.getInstance().setUserId(response.getId());
                UserSession.getInstance().setUsername(response.getUsername());
                UserSession.getInstance().setEmail(response.getEmail());

                // ABRIR DASHBOARD (Le pasamos el stage actual para que cambie la vista)
                new MainFrame(stage); 
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                // Mostrar alerta de error estilo JavaFX
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de acceso");
                alert.setHeaderText(null);
                
                // Limpiar el mensaje de la excepción para que sea legible
                String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                alert.setContentText("No se pudo iniciar sesión: " + errorMsg);
                
                alert.showAndWait();

                loginButton.setDisable(false);
                loginButton.setText("Iniciar Sesión");
            });
            return null;
        });
    }
}