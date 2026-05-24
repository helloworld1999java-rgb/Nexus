package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.service.ProductService;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import com.marketplace.util.ImageLoader;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private HBox categoryChipsBox;
    @FXML private FlowPane featuredFlowPane;

    private final ProductService productService = new ProductService();

    public static Product selectedProduct;

    @FXML
    public void initialize() {
        loadCategories();
        loadFeatured();
    }

    private void loadCategories() {
        AsyncTask.run(
            () -> productService.getCategories(),
            categories -> {
                categoryChipsBox.getChildren().clear();
                for (String cat : categories) {
                    Button chip = new Button(cat);
                    chip.getStyleClass().add("category-chip");
                    chip.setOnAction(e -> MainController.getInstance().loadContent("catalog"));
                    categoryChipsBox.getChildren().add(chip);
                }
            },
            ex -> {}
        );
    }

    private void loadFeatured() {
        AsyncTask.run(
            () -> productService.getFeaturedProducts(),
            products -> {
                featuredFlowPane.getChildren().clear();
                for (Product p : products) featuredFlowPane.getChildren().add(createProductCard(p));
            },
            ex -> {}
        );
    }

    @FXML
    private void onShowCatalog() {
        MainController.getInstance().loadContent("catalog");
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");

        ImageView iv = new ImageView(ImageLoader.load(product.getFirstImageUrl()));
        iv.setFitWidth(220);
        iv.setFitHeight(180);
        iv.setPreserveRatio(false);

        VBox info = new VBox(6);
        info.setPadding(new Insets(10, 14, 14, 14));

        Label name = new Label(product.getName());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);
        name.setMaxWidth(192);

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(FormatUtil.formatPrice(product.getPrice()));
        price.getStyleClass().add("price-label");
        priceRow.getChildren().add(price);

        if (product.getOldPrice() != null && product.getOldPrice().compareTo(product.getPrice()) > 0) {
            Label oldPrice = new Label(FormatUtil.formatPrice(product.getOldPrice()));
            oldPrice.getStyleClass().add("price-old");
            priceRow.getChildren().add(oldPrice);
        }

        info.getChildren().addAll(name, priceRow);
        card.getChildren().addAll(iv, info);

        card.setOnMouseClicked(e -> {
            selectedProduct = product;
            MainController.getInstance().loadContent("productDetail");
        });
        return card;
    }
}
