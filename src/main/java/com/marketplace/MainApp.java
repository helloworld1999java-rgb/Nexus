package com.marketplace;

import com.marketplace.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NEXUS Marketplace");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);

        SceneManager.getInstance().setPrimaryStage(primaryStage);
        SceneManager.getInstance().switchTo("main");

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
