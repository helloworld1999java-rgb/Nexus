package com.marketplace.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private final Map<String, String> scenes = new HashMap<>();

    private SceneManager() {
        scenes.put("auth",    "/com/marketplace/fxml/AuthView.fxml");
        scenes.put("main",    "/com/marketplace/fxml/MainView.fxml");
        scenes.put("catalog", "/com/marketplace/fxml/CatalogView.fxml");
        scenes.put("product", "/com/marketplace/fxml/ProductDetailView.fxml");
        scenes.put("cart",    "/com/marketplace/fxml/CartView.fxml");
        scenes.put("checkout","/com/marketplace/fxml/CheckoutView.fxml");
        scenes.put("profile", "/com/marketplace/fxml/ProfileView.fxml");
        scenes.put("orders",  "/com/marketplace/fxml/OrdersView.fxml");
        scenes.put("seller",  "/com/marketplace/fxml/SellerView.fxml");
        scenes.put("admin",   "/com/marketplace/fxml/AdminView.fxml");
    }

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public void setPrimaryStage(Stage stage) { this.primaryStage = stage; }
    public Stage getPrimaryStage()           { return primaryStage; }

    public void switchTo(String sceneName) { switchTo(sceneName, null); }

    public void switchTo(String sceneName, Object data) {
        try {
            String fxmlPath = scenes.get(sceneName);
            if (fxmlPath == null) throw new IllegalArgumentException("Unknown scene: " + sceneName);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (data != null && loader.getController() instanceof DataReceiver) {
                ((DataReceiver) loader.getController()).receiveData(data);
            }

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1280, 800);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            applyTheme(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Ошибка навигации", e.getMessage());
        }
    }

    public void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        String css = getClass().getResource("/com/marketplace/css/main.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    public <T> FXMLLoader getLoader(String fxmlPath) {
        return new FXMLLoader(getClass().getResource(fxmlPath));
    }

    public interface DataReceiver {
        void receiveData(Object data);
    }
}
