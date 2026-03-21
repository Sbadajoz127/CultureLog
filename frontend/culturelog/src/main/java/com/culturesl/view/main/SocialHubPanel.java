package com.culturesl.view.main;

import com.culturesl.api.ApiClient;
import com.culturesl.dto.PostResponse;
import com.culturesl.view.utils.ModernStyles;

import com.culturesl.view.components.RichPostCard; 

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SocialHubPanel extends BorderPane {

    public SocialHubPanel() {
        this.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + ";");

        // A. SIDEBAR IZQUIERDA (Navegación)
        this.setLeft(createLeftSidebar());

        // B. CENTRO (Feed + Tendencias)
        this.setCenter(createCenterScrollable());

        // C. SIDEBAR DERECHA (Recomendados)
        this.setRight(createRightSidebar());
    }

    // --- A. BARRA LATERAL IZQUIERDA ---
    private VBox createLeftSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(20, 20, 20, 20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + "; -fx-border-color: #333; -fx-border-width: 0 1 0 0;"); // Borde derecho sutil

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(logoBox, new Insets(0, 0, 40, 0));

        try {
            URL imgUrl = getClass().getResource("/res/CultureLog_Blanco.png");
            if (imgUrl != null) {
                ImageView logoImg = new ImageView(new Image(imgUrl.toString()));
                logoImg.setFitWidth(95);
                logoImg.setFitHeight(60);
                logoImg.setPreserveRatio(true);
                logoBox.getChildren().add(logoImg);
            } else {
                Label logoText = new Label("MediaHub");
                logoText.setFont(ModernStyles.FONT_TITLE);
                logoText.setTextFill(Color.WHITE);
                logoBox.getChildren().add(logoText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sidebar.getChildren().add(logoBox);

        // Menú
        String[] menuItems = {"Películas", "Libros", "Música", "Fotografía", "Mi Colección", "Listas", "Comunidad"};

        for (String item : menuItems) {
            Button btn = new Button(item);
            btn.setFont(ModernStyles.FONT_NORMAL);
            btn.setTextFill(ModernStyles.TEXT_SECONDARY);
            btn.setStyle("-fx-background-color: transparent; -fx-alignment: center-left; -fx-cursor: hand;");
            btn.setMaxWidth(Double.MAX_VALUE); // Para que ocupe todo el ancho
            
            // Efecto Hover
            btn.setOnMouseEntered(e -> {
                btn.setTextFill(Color.WHITE);
                btn.setStyle("-fx-background-color: #2a2a2a; -fx-alignment: center-left; -fx-cursor: hand; -fx-background-radius: 5;");
            });
            btn.setOnMouseExited(e -> {
                btn.setTextFill(ModernStyles.TEXT_SECONDARY);
                btn.setStyle("-fx-background-color: transparent; -fx-alignment: center-left; -fx-cursor: hand;");
            });

            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    // --- B. CENTRO (Feed y Tendencias) ---
    private ScrollPane createCenterScrollable() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 40, 20, 40));
        content.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + ";");

        // 1. Sección "Tendencias de Hoy"
        Label lblTrends = new Label("Explora & Descubre");
        lblTrends.setFont(ModernStyles.FONT_TITLE);
        lblTrends.setTextFill(Color.WHITE);
        VBox.setMargin(lblTrends, new Insets(0, 0, 15, 0));

        HBox trendsPanel = new HBox(15);
        trendsPanel.setAlignment(Pos.CENTER_LEFT);
        
        // Añadir tarjetas de tendencias
        trendsPanel.getChildren().addAll(
            createTrendCard("Dune", "4.5", Color.rgb(50, 40, 30)),
            createTrendCard("El Gran Gatsby", "Book", Color.rgb(30, 40, 50)),
            createTrendCard("Pink Floyd", "Music", Color.rgb(20, 20, 20))
        );
        VBox.setMargin(trendsPanel, new Insets(0, 0, 30, 0));

        content.getChildren().addAll(lblTrends, trendsPanel);

        // 2. Feed de Posts
        Label lblFeed = new Label("Últimas Reseñas");
        lblFeed.setFont(ModernStyles.FONT_HEADER);
        lblFeed.setTextFill(Color.WHITE);
        VBox.setMargin(lblFeed, new Insets(0, 0, 15, 0));
        content.getChildren().add(lblFeed);

        // Contenedor donde se cargarán los posts asíncronamente
        VBox feedContainer = new VBox(20);
        content.getChildren().add(feedContainer);

        // Cargar posts reales
        loadPosts(feedContainer);

        // Envolver en un ScrollPane
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true); // Hace que el contenido se expanda al ancho
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + ";");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Ocultar scroll horizontal

        return scroll;
    }

    private StackPane createTrendCard(String title, String subtitle, Color bgColor) {
        StackPane card = new StackPane();
        card.setPrefSize(200, 160);
        card.setStyle("-fx-background-color: " + ModernStyles.toHex(bgColor) + "; -fx-background-radius: 15;");

        VBox texts = new VBox(5);
        texts.setAlignment(Pos.BOTTOM_LEFT);
        texts.setPadding(new Insets(15));

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        
        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 10px;");

        texts.getChildren().addAll(lblTitle, lblSub);
        card.getChildren().add(texts);

        return card;
    }

    // --- C. SIDEBAR DERECHA (Recomendados) ---
    private VBox createRightSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_MAIN) + ";");

        // Barra de búsqueda simulada
        TextField searchBar = new TextField();
        searchBar.setPromptText("🔍 Buscar...");
        searchBar.setPrefHeight(35);
        searchBar.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_PANEL) + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-border-color: transparent;");
        VBox.setMargin(searchBar, new Insets(0, 0, 30, 0));

        // Lista "Recomendado para Ti"
        Label lblRec = new Label("Recomendado para Ti");
        lblRec.setFont(ModernStyles.FONT_HEADER);
        lblRec.setTextFill(Color.WHITE);
        VBox.setMargin(lblRec, new Insets(0, 0, 15, 0));

        sidebar.getChildren().addAll(searchBar, lblRec);

        // Items pequeños
        sidebar.getChildren().addAll(
            createMiniItem("Los Siete Samuráis", "Película"),
            createMiniItem("1984", "George Orwell"),
            createMiniItem("Dark Side of the Moon", "Pink Floyd")
        );

        return sidebar;
    }

    private HBox createMiniItem(String title, String subtitle) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(itemBox, new Insets(0, 0, 15, 0));

        // Icono cuadrado simulado
        Rectangle icon = new Rectangle(40, 40);
        icon.setArcWidth(10);
        icon.setArcHeight(10);
        icon.setFill(Color.DARKGRAY);

        VBox texts = new VBox();
        Label l1 = new Label(title);
        l1.setTextFill(Color.WHITE);
        l1.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label l2 = new Label(subtitle);
        l2.setTextFill(ModernStyles.TEXT_SECONDARY);
        l2.setFont(ModernStyles.FONT_SMALL);

        texts.getChildren().addAll(l1, l2);
        itemBox.getChildren().addAll(icon, texts);

        return itemBox;
    }

    // --- LÓGICA DE CARGA DE POSTS ---
    private void loadPosts(VBox container) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // AQUÍ USAS TU API REAL.
                return ApiClient.getInstance().getFeed(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(posts -> {
            Platform.runLater(() -> {
                for (PostResponse p : posts) {
                    // TODO: Reemplazar este Label temporal por tu RichPostCard(p) cuando lo migremos a JavaFX
                    RichPostCard card = new RichPostCard(p);
                    container.getChildren().add(card);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("Error cargando el feed: " + ex.getMessage());
            });
            return null;
        });
    }
}