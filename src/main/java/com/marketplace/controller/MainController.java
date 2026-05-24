package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.User;
import com.marketplace.service.AuthService;
import com.marketplace.service.CartService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private Button homeBtn;
    @FXML private Button catalogBtn;
    @FXML private Button cartBtn;
    @FXML private Button ordersBtn;
    @FXML private Button profileBtn;
    @FXML private Button sellerBtn;
    @FXML private Button adminBtn;
    @FXML private Label  cartBadgeLabel;

    @FXML private HBox authPanel;
    @FXML private HBox guestPanel;

    private final CartService cartService = new CartService();
    private final AuthService authService = new AuthService();

    private static final Map<String, String> VIEWS = new HashMap<>();

    private static final java.util.Set<String> NO_CACHE_VIEWS =
            java.util.Set.of("profile", "seller", "admin", "orders", "cart", "checkout");

    private final Map<String, Parent> viewCache = new HashMap<>();

    static {
        VIEWS.put("home",          "/com/marketplace/fxml/HomeView.fxml");
        VIEWS.put("catalog",       "/com/marketplace/fxml/CatalogView.fxml");
        VIEWS.put("productDetail", "/com/marketplace/fxml/ProductDetailView.fxml");
        VIEWS.put("cart",          "/com/marketplace/fxml/CartView.fxml");
        VIEWS.put("checkout",      "/com/marketplace/fxml/CheckoutView.fxml");
        VIEWS.put("orders",        "/com/marketplace/fxml/OrdersView.fxml");
        VIEWS.put("profile",       "/com/marketplace/fxml/ProfileView.fxml");
        VIEWS.put("seller",        "/com/marketplace/fxml/SellerView.fxml");
        VIEWS.put("admin",         "/com/marketplace/fxml/AdminView.fxml");
    }

    private static MainController instance;
    public static MainController getInstance() { return instance; }

    @FXML
    public void initialize() {
        instance = this;
        updateNavVisibility();
        updateCartBadge();
        doLoadContent("home");
        startBlockStatusChecker();
    }

    private void startBlockStatusChecker() {
        Thread checker = new Thread(() -> {
            while (true) {
                try { Thread.sleep(30_000); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); break;
                }
                User current = AppState.getInstance().getCurrentUser();
                if (current == null) continue;
                try {
                    User fresh = authService.getUserById(current.getId());
                    if (fresh == null) continue;
                    if (fresh.isBlocked()) {
                        javafx.application.Platform.runLater(() -> {
                            AppState.getInstance().logout();
                            AlertUtil.showError("Аккаунт заблокирован",
                                    "Ваш аккаунт был заблокирован администратором.");
                            SceneManager.getInstance().switchTo("auth");
                        });
                        break;
                    }
                    AppState.getInstance().setCurrentUser(fresh);
                } catch (Exception ignored) {}
            }
        });
        checker.setDaemon(true);
        checker.setName("block-status-checker");
        checker.start();
    }

    public void updateNavVisibility() {
        User user = AppState.getInstance().getCurrentUser();
        boolean loggedIn = user != null;
        boolean isSeller = loggedIn && ("seller".equals(user.getRole()) || "admin".equals(user.getRole()));
        boolean isAdmin  = loggedIn && "admin".equals(user.getRole());

        sellerBtn.setVisible(isSeller);  sellerBtn.setManaged(isSeller);
        adminBtn.setVisible(isAdmin);    adminBtn.setManaged(isAdmin);

        cartBtn.setVisible(loggedIn);    cartBtn.setManaged(loggedIn);
        ordersBtn.setVisible(loggedIn);  ordersBtn.setManaged(loggedIn);
        profileBtn.setVisible(loggedIn); profileBtn.setManaged(loggedIn);

        guestPanel.setVisible(!loggedIn);  guestPanel.setManaged(!loggedIn);
        authPanel.setVisible(loggedIn);    authPanel.setManaged(loggedIn);
    }

    public void updateCartBadge() {
        cartBadgeLabel.setVisible(false);
    }

    public void loadContent(String viewName) {
        doLoadContent(viewName);
    }

    public void invalidateCache(String viewName) { viewCache.remove(viewName); }
    public void invalidateAllCache()             { viewCache.clear(); }

    private void doLoadContent(String viewName) {
        String path = VIEWS.get(viewName);
        if (path == null) { AlertUtil.showError("Ошибка", "Вид не найден: " + viewName); return; }
        try {
            Parent view;
            if (!NO_CACHE_VIEWS.contains(viewName) && viewCache.containsKey(viewName)) {
                view = viewCache.get(viewName);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                view = loader.load();
                if (!NO_CACHE_VIEWS.contains(viewName)) viewCache.put(viewName, view);
            }
            rootPane.setCenter(view);
            updateActiveButton(viewName);
        } catch (Exception e) {
            AlertUtil.showError("Ошибка загрузки", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateActiveButton(String viewName) {
        for (Button b : new Button[]{homeBtn, catalogBtn, cartBtn, ordersBtn, profileBtn, sellerBtn, adminBtn}) {
            b.getStyleClass().remove("nav-link-active");
        }
        switch (viewName) {
            case "home"    -> homeBtn.getStyleClass().add("nav-link-active");
            case "catalog" -> catalogBtn.getStyleClass().add("nav-link-active");
            case "cart"    -> cartBtn.getStyleClass().add("nav-link-active");
            case "orders"  -> ordersBtn.getStyleClass().add("nav-link-active");
            case "profile" -> profileBtn.getStyleClass().add("nav-link-active");
            case "seller"  -> sellerBtn.getStyleClass().add("nav-link-active");
            case "admin"   -> adminBtn.getStyleClass().add("nav-link-active");
        }
    }

    @FXML private void onHome()     { loadContent("home"); }
    @FXML private void onCatalog()  { loadContent("catalog"); }
    @FXML private void onCart()     { loadContent("cart"); }
    @FXML private void onOrders()   { loadContent("orders"); }
    @FXML private void onProfile()  { loadContent("profile"); }
    @FXML private void onSeller()   { loadContent("seller"); }
    @FXML private void onAdmin()    { loadContent("admin"); }

    @FXML
    private void onLogin() {
        SceneManager.getInstance().switchTo("auth");
    }

    @FXML
    private void onRegister() {
        AppState.getInstance().setOpenRegistration(true);
        SceneManager.getInstance().switchTo("auth");
    }

    @FXML
    private void onLogout() {
        AsyncTask.run(
                () -> { authService.signOut(); return null; },
                v  -> {
                    invalidateAllCache();
                    AppState.getInstance().logout();
                    updateNavVisibility();
                    SceneManager.getInstance().switchTo("auth");
                },
                ex -> {
                    invalidateAllCache();
                    AppState.getInstance().logout();
                    updateNavVisibility();
                    SceneManager.getInstance().switchTo("auth");
                }
        );
    }
}
