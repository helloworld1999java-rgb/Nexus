package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.service.ProductService;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import com.marketplace.util.ImageLoader;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.math.BigDecimal;

public class CatalogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Slider minPriceSlider;
    @FXML private Slider maxPriceSlider;
    @FXML private Label minPriceLabel;
    @FXML private Label maxPriceLabel;
    @FXML private CheckBox ratingFilterCheck;
    @FXML private FlowPane productsFlowPane;
    @FXML private HBox paginationBox;
    @FXML private Label resultsLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final ProductService productService = new ProductService();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    @FXML
    public void initialize() {
        sortCombo.getItems().addAll("По популярности", "Сначала дешевле", "Сначала дороже", "Новинки");
        sortCombo.setValue("По популярности");
        sortCombo.setOnAction(e -> { currentPage = 0; loadProducts(); });

        minPriceSlider.valueProperty().addListener((o, ov, nv) ->
            minPriceLabel.setText(FormatUtil.formatPrice(BigDecimal.valueOf(nv.doubleValue()))));
        maxPriceSlider.valueProperty().addListener((o, ov, nv) ->
            maxPriceLabel.setText(FormatUtil.formatPrice(BigDecimal.valueOf(nv.doubleValue()))));

        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        AsyncTask.run(
            () -> productService.getCategories(),
            cats -> {
                categoryCombo.getItems().clear();
                categoryCombo.getItems().add("Все категории");
                categoryCombo.getItems().addAll(cats);
                categoryCombo.setValue("Все категории");
                categoryCombo.setOnAction(e -> { currentPage = 0; loadProducts(); });
            },
            ex -> {}
        );
    }

    @FXML private void onSearch() { currentPage = 0; loadProducts(); }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        sortCombo.setValue("По популярности");
        categoryCombo.setValue("Все категории");
        minPriceSlider.setValue(minPriceSlider.getMin());
        maxPriceSlider.setValue(maxPriceSlider.getMax());
        ratingFilterCheck.setSelected(false);
        currentPage = 0;
        loadProducts();
    }

    private void loadProducts() {
        loadingIndicator.setVisible(true);
        productsFlowPane.getChildren().clear();

        String search   = searchField.getText().trim();
        String cat      = categoryCombo.getValue();
        String category = (cat == null || cat.equals("Все категории")) ? null : cat;
        String[] sort   = mapSort(sortCombo.getValue());
        Double minPrice = minPriceSlider.getValue() > 0 ? minPriceSlider.getValue() : null;
        Double maxPrice = maxPriceSlider.getValue() < maxPriceSlider.getMax() ? maxPriceSlider.getValue() : null;
        Double minRating = ratingFilterCheck.isSelected() ? 4.0 : null;

        AsyncTask.run(
            () -> productService.getProducts(
                category, search, sort[0], sort[1],
                minPrice, maxPrice, minRating,
                PAGE_SIZE, currentPage * PAGE_SIZE),
            products -> {
                loadingIndicator.setVisible(false);
                productsFlowPane.getChildren().clear();
                for (Product p : products) productsFlowPane.getChildren().add(createCard(p));
                resultsLabel.setText("Найдено: " + products.size());
                buildPagination(products.size());
            },
            ex -> loadingIndicator.setVisible(false)
        );
    }

    private VBox createCard(Product product) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");

        ImageView iv = new ImageView(ImageLoader.load(product.getFirstImageUrl()));
        iv.setFitWidth(220);
        iv.setFitHeight(170);
        iv.setPreserveRatio(false);

        VBox info = new VBox(6);
        info.setPadding(new Insets(10, 12, 14, 12));

        Label name = new Label(product.getName());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);
        name.setMaxWidth(196);

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label price = new Label(FormatUtil.formatPrice(product.getPrice()));
        price.getStyleClass().add("price-label");
        priceRow.getChildren().add(price);

        if (product.getOldPrice() != null && product.getOldPrice().compareTo(product.getPrice()) > 0) {
            Label old = new Label(FormatUtil.formatPrice(product.getOldPrice()));
            old.getStyleClass().add("price-old");
            priceRow.getChildren().add(old);
        }

        info.getChildren().addAll(name, priceRow);
        card.getChildren().addAll(iv, info);

        card.setOnMouseClicked(e -> {
            HomeController.selectedProduct = product;
            MainController.getInstance().loadContent("productDetail");
        });
        return card;
    }

    private void buildPagination(int count) {
        paginationBox.getChildren().clear();
        if (currentPage > 0) {
            Button prev = new Button("← Назад");
            prev.getStyleClass().add("btn-outline");
            prev.setOnAction(e -> { currentPage--; loadProducts(); });
            paginationBox.getChildren().add(prev);
        }
        Label pageLabel = new Label("Стр. " + (currentPage + 1));
        pageLabel.setStyle("-fx-padding: 8 12; -fx-text-fill: -color-text-2;");
        paginationBox.getChildren().add(pageLabel);
        if (count == PAGE_SIZE) {
            Button next = new Button("Вперёд →");
            next.getStyleClass().add("btn-outline");
            next.setOnAction(e -> { currentPage++; loadProducts(); });
            paginationBox.getChildren().add(next);
        }
    }

    private String[] mapSort(String label) {
        if (label == null) return new String[]{"created_at", "desc"};
        return switch (label) {
            case "Сначала дешевле" -> new String[]{"price",       "asc"};
            case "Сначала дороже"  -> new String[]{"price",       "desc"};
            case "Новинки"         -> new String[]{"created_at",  "desc"};
            default                -> new String[]{"sales_count", "desc"};
        };
    }
}
