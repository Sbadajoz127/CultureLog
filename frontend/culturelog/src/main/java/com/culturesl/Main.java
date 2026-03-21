package com.culturesl;

import com.culturesl.view.login.LoginFrame;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Le pasamos el escenario principal al Login
        new LoginFrame(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}