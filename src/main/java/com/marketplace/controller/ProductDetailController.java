package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.Product;
import com.marketplace.service.CartService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import com.marketplace.util.ImageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

public class ProductDetailController {

    @FXML private ImageView mainImage;
    @FXML private HBox thumbnailsBox;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Label productOldPrice;
    @FXML private Label productDescription;
    @FXML private Label stockLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartBtn;
    @FXML private Button buyNowBtn;
    @FXML private Label brandLabel;
    @FXML private Label articleLabel;
    @FXML private Label supplierCountryLabel;
    @FXML private HBox  deliveryBox;
    @FXML private Label deliveryLabel;

    private final CartService cartService = new CartService();
    private Product currentProduct;

    @FXML
    public void initialize() {
        currentProduct = HomeController.selectedProduct;
        if (currentProduct == null) return;
        fillData();
    }

    private void fillData() {
        Product p = currentProduct;

        productName.setText(p.getName());
        productPrice.setText(FormatUtil.formatPrice(p.getPrice()));

        if (p.getOldPrice() != null && p.getOldPrice().compareTo(p.getPrice()) > 0) {
            productOldPrice.setText(FormatUtil.formatPrice(p.getOldPrice()));
            productOldPrice.setVisible(true);
            productOldPrice.setManaged(true);
        } else {
            productOldPrice.setVisible(false);
            productOldPrice.setManaged(false);
        }

        productDescription.setText(p.getDescription() != null ? p.getDescription() : "");

        if (p.getArticle() != null && !p.getArticle().isEmpty()) {
            articleLabel.setText("Артикул: " + p.getArticle());
            articleLabel.setVisible(true);
            articleLabel.setManaged(true);
        } else {
            articleLabel.setVisible(false);
            articleLabel.setManaged(false);
        }

        if (p.getBrand() != null && !p.getBrand().isEmpty()) {
            brandLabel.setText("Бренд: " + p.getBrand());
            brandLabel.setVisible(true);
            brandLabel.setManaged(true);
        } else {
            brandLabel.setVisible(false);
            brandLabel.setManaged(false);
        }

        if (p.getSupplierCountry() != null && !p.getSupplierCountry().isEmpty()) {
            supplierCountryLabel.setText("Страна поставщика: " + p.getSupplierCountry());
            supplierCountryLabel.setVisible(true);
            supplierCountryLabel.setManaged(true);
        } else {
            supplierCountryLabel.setVisible(false);
            supplierCountryLabel.setManaged(false);
        }

        int stock = p.getStock();
        if (stock <= 0) {
            stockLabel.setText("Нет в наличии");
            stockLabel.setStyle("-fx-text-fill: -color-danger; -fx-font-weight: bold;");
            addToCartBtn.setDisable(true);
            buyNowBtn.setDisable(true);
        } else if (stock <= 5) {
            stockLabel.setText("Осталось: " + stock + " шт.");
            stockLabel.setStyle("-fx-text-fill: -color-warning; -fx-font-weight: bold;");
        } else {
            stockLabel.setText("В наличии");
            stockLabel.setStyle("-fx-text-fill: -color-success; -fx-font-weight: bold;");
        }

        int dMin = p.getDeliveryDaysMin() != null ? p.getDeliveryDaysMin() : 3;
        int dMax = p.getDeliveryDaysMax() != null ? p.getDeliveryDaysMax() : 7;
        deliveryLabel.setText("Доставка: " + p.getDeliveryLabel());
        deliveryBox.setVisible(true);
        deliveryBox.setManaged(true);

        SpinnerValueFactory<Integer> factory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Math.max(1, stock), 1);
        quantitySpinner.setValueFactory(factory);

        loadImages(p.getImageUrls());
    }

    private void loadImages(List<String> urls) {
        thumbnailsBox.getChildren().clear();

        if (urls == null || urls.isEmpty()) {
            mainImage.setImage(ImageLoader.loadLarge(null));
            return;
        }

        mainImage.setImage(ImageLoader.loadLarge(urls.get(0)));

        for (int i = 0; i < urls.size(); i++) {
            final String url = urls.get(i);

            StackPane wrap = new StackPane();
            wrap.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8px; -fx-padding: 4; -fx-cursor: hand;");
            wrap.setPrefSize(70, 70);

            ImageView thumb = new ImageView(ImageLoader.load(url));
            thumb.setFitWidth(62);
            thumb.setFitHeight(62);
            thumb.setPreserveRatio(false);
            wrap.getChildren().add(thumb);

            if (i == 0) {
                wrap.setStyle("-fx-background-color: -color-primary-soft; -fx-background-radius: 8px; -fx-border-color: -color-primary; -fx-border-radius: 8px; -fx-border-width: 1.5px; -fx-padding: 4; -fx-cursor: hand;");
            }

            wrap.setOnMouseClicked(e -> {
                mainImage.setImage(ImageLoader.loadLarge(url));
                thumbnailsBox.getChildren().forEach(n ->
                    n.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8px; -fx-padding: 4; -fx-cursor: hand;"));
                wrap.setStyle("-fx-background-color: -color-primary-soft; -fx-background-radius: 8px; -fx-border-color: -color-primary; -fx-border-radius: 8px; -fx-border-width: 1.5px; -fx-padding: 4; -fx-cursor: hand;");
            });

            thumbnailsBox.getChildren().add(wrap);
        }
    }

    @FXML
    private void onAddToCart() {
        if (AppState.getInstance().getCurrentUser() == null) {
            AlertUtil.showError("Ошибка", "Войдите в аккаунт"); return;
        }
        int qty = quantitySpinner.getValue();
        addToCartBtn.setDisable(true);
        AsyncTask.run(
            () -> { cartService.addToCart(currentProduct.getId(), qty); return null; },
            v -> {
                addToCartBtn.setDisable(false);
                AlertUtil.showInfo("Корзина", "Товар добавлен в корзину!");
                MainController.getInstance().updateCartBadge();
            },
            ex -> { addToCartBtn.setDisable(false); AlertUtil.showError("Ошибка", ex.getMessage()); }
        );
    }

    @FXML
    private void onBuyNow() {
        if (AppState.getInstance().getCurrentUser() == null) {
            AlertUtil.showError("Ошибка", "Войдите в аккаунт"); return;
        }
        AsyncTask.run(
            () -> { cartService.addToCart(currentProduct.getId(), quantitySpinner.getValue()); return null; },
            v -> MainController.getInstance().loadContent("checkout"),
            ex -> AlertUtil.showError("Ошибка", ex.getMessage())
        );
    }

    @FXML
    private void onBack() {
        MainController.getInstance().loadContent("catalog");
    }
}
