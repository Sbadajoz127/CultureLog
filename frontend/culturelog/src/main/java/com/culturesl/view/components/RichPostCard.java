package com.culturesl.view.components;

import com.culturesl.dto.PostResponse;
import com.culturesl.view.utils.ModernStyles;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class RichPostCard extends VBox {

    public RichPostCard(PostResponse post) {
        // Configuramos la tarjeta principal (VBox)
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        // Bordes redondeados de 20px y color de fondo del panel
        this.setStyle("-fx-background-color: " + ModernStyles.toHex(ModernStyles.BG_PANEL) + "; -fx-background-radius: 20;");
        this.setMaxWidth(Double.MAX_VALUE); // Para que se expanda en el scroll

        // --- 1. CABECERA (Avatar + Nombre) ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar simulado (Círculo)
        Circle avatar = new Circle(20); // Radio de 20 = diámetro de 40
        avatar.setFill(Color.GRAY);

        // Textos del autor
        VBox texts = new VBox(2);
        
        Label lblName = new Label(post.getAuthorName() != null ? post.getAuthorName() : "Usuario Desconocido");
        lblName.setFont(ModernStyles.FONT_HEADER);
        lblName.setTextFill(ModernStyles.TEXT_PRIMARY);

        Label lblAction = new Label("ha publicado una reseña");
        lblAction.setFont(ModernStyles.FONT_SMALL);
        lblAction.setTextFill(ModernStyles.TEXT_SECONDARY);

        texts.getChildren().addAll(lblName, lblAction);
        header.getChildren().addAll(avatar, texts);
        
        this.getChildren().add(header);

        // --- 2. CONTENIDO VISUAL ---
        
        // Texto de la reseña
        Text txtContent = new Text(post.getContent() != null ? post.getContent() : "");
        txtContent.setFont(ModernStyles.FONT_NORMAL);
        txtContent.setFill(ModernStyles.TEXT_PRIMARY);
        txtContent.setWrappingWidth(500); // Esto hace que el texto haga salto de línea automático
        
        this.getChildren().add(txtContent);

        // Si hay película/libro vinculado, dibujamos el banner GRANDE
        if (post.getLinkedItemTitle() != null && !post.getLinkedItemTitle().isEmpty()) {
            
            // Creamos un StackPane para el Banner (permite poner cosas encima del fondo)
            StackPane banner = new StackPane();
            banner.setPrefHeight(180);
            banner.setMinHeight(180); // Aseguramos la altura fija
            
            // Simulamos el fondo con un gradiente como tenías en Swing
            LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 50, 0)),
                new Stop(1, Color.rgb(20, 10, 0))
            );
            
            // Aplicamos el gradiente al fondo del StackPane con bordes redondeados
            BackgroundFill backgroundFill = new BackgroundFill(gradient, new CornerRadii(15), Insets.EMPTY);
            banner.setBackground(new Background(backgroundFill));
            
            // Contenedor interno para organizar el título y el rating
            VBox bannerTexts = new VBox(5);
            bannerTexts.setAlignment(Pos.CENTER_LEFT);
            bannerTexts.setPadding(new Insets(15));
            
            Label lblTitle = new Label(post.getLinkedItemTitle().toUpperCase());
            lblTitle.setFont(ModernStyles.FONT_TITLE);
            lblTitle.setTextFill(Color.WHITE);
            // Hacer que el título se ajuste si es muy largo
            lblTitle.setWrapText(true); 
            
            // Para prevenir NullPointerException si getLinkedItemRating devuelve un entero primitivo o nulo
            String ratingValue = "?";
            try {
                ratingValue = String.valueOf(post.getLinkedItemRating());
            } catch (Exception e) {}

            Label lblRating = new Label("★ " + ratingValue + "/5");
            lblRating.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffc107;"); // Dorado

            bannerTexts.getChildren().addAll(lblTitle, lblRating);
            banner.getChildren().add(bannerTexts);
            
            this.getChildren().add(banner);
        }

        // --- 3. FOOTER (Likes y Comentarios) ---
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label lblLikes = createStatIcon("♥ " + post.getLikeCount());
        Label lblComments = createStatIcon("💬 " + post.getCommentCount() + " comentarios");
        
        footer.getChildren().addAll(lblLikes, lblComments);
        
        this.getChildren().add(footer);
    }

    private Label createStatIcon(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(ModernStyles.TEXT_SECONDARY);
        lbl.setFont(ModernStyles.FONT_SMALL);
        return lbl;
    }
}