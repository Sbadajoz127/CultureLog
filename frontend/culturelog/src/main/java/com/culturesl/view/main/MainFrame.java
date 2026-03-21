package com.culturesl.view.main;

import com.culturesl.model.UserSession;
import com.culturesl.view.login.LoginFrame;
import com.culturesl.view.profile.ProfilePanel;
import com.culturesl.view.utils.ModernStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainFrame { 

    private Stage stage;
    private StackPane contentArea;
    
    // Referencias a los paneles reales
    private SocialHubPanel socialHubPanel;
    private ProfilePanel profilePanel;

    public MainFrame(Stage stage) {
        this.stage = stage;
        initComponents();
    }

    private void initComponents() {
        BorderPane root = new BorderPane();
        // Usamos el color de fondo de tu clase ModernStyles
        root.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + ";");

        // 1. TOOLBAR SUPERIOR (HBox)
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        // Estilo oscuro para la barra con un borde inferior sutil
        toolbar.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");

        // Botones de navegación
        Button btnFeed = createNavButton("Feed Social");
        Button btnLibrary = createNavButton("Mi Biblioteca");
        Button btnMisPublicaciones = createNavButton("Mis Publicaciones");
        Button btnProfile = createNavButton("Perfil");

        // Espaciador para empujar el nombre de usuario y logout a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Información de usuario
        String username = (UserSession.getInstance() != null) ? UserSession.getInstance().getUsername() : "Usuario";
        Label lblUser = new Label("Hola, " + username + "  ");
        lblUser.setTextFill(Color.WHITE);
        lblUser.setFont(ModernStyles.FONT_NORMAL);

        // Botón de Salir (Rojo)
        Button btnLogout = createNavButton("Salir");
        btnLogout.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5px; -fx-font-weight: bold;");
        btnLogout.setOnAction(e -> logout());

        toolbar.getChildren().addAll(btnFeed, btnLibrary, btnMisPublicaciones, spacer, lblUser, btnProfile, btnLogout);
        root.setTop(toolbar);

        // 2. PANEL CENTRAL (StackPane - Equivalente a CardLayout)
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + ";");

        // --- CARGA DE PANELES REALES ---
        socialHubPanel = new SocialHubPanel();
        profilePanel = new ProfilePanel(stage);

        // Placeholders para las secciones que aún no has migrado
        Label lblLibrary = createPlaceholder("AQUÍ IRÁ LA BIBLIOTECA MULTIMEDIA");
        Label lblPubs = createPlaceholder("AQUÍ IRÁ EL LISTADO DE TUS PUBLICACIONES");

        // Añadimos todos al contenedor (el último añadido queda "arriba", pero controlaremos visibilidad)
        contentArea.getChildren().addAll(socialHubPanel, profilePanel, lblLibrary, lblPubs);

        // 3. LÓGICA DE NAVEGACIÓN
        btnFeed.setOnAction(e -> showPanel(socialHubPanel));
        btnProfile.setOnAction(e -> showPanel(profilePanel));
        btnLibrary.setOnAction(e -> showPanel(lblLibrary));
        btnMisPublicaciones.setOnAction(e -> showPanel(lblPubs));

        // Mostrar el Feed Social por defecto al entrar
        showPanel(socialHubPanel);

        root.setCenter(contentArea);

        // Configuración de la Escena
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("CultureLog - " + username);
        stage.setScene(scene);
        stage.setResizable(true); // Permitimos redimensionar la ventana principal
        stage.show();
    }

    /**
     * Helper para cambiar entre paneles de forma limpia
     */
    private void showPanel(javafx.scene.Node panelToShow) {
        // Ocultamos todos los hijos del StackPane
        contentArea.getChildren().forEach(node -> node.setVisible(false));
        // Mostramos solo el seleccionado
        panelToShow.setVisible(true);
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setFont(ModernStyles.FONT_NORMAL);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
        
        // Efecto visual al pasar el ratón (Hover)
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5px; -fx-padding: 8 15 8 15;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-cursor: hand; -fx-padding: 8 15 8 15;"));
        
        return btn;
    }

    private Label createPlaceholder(String text) {
        Label label = new Label(text);
        label.setFont(ModernStyles.FONT_TITLE);
        label.setTextFill(ModernStyles.TEXT_SECONDARY);
        label.setVisible(false); // Empiezan ocultos
        return label;
    }

    private void logout() {
        if (UserSession.getInstance() != null) {
            UserSession.getInstance().cleanSession();
        }
        // Volvemos a la pantalla de Login pasándole el Stage
        new LoginFrame(stage);
    }
}