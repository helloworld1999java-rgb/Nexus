package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.Product;
import com.marketplace.service.ProductService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import com.marketplace.util.ImageLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SellerController {

    @FXML private Label totalProductsLabel;
    @FXML private Label totalSalesLabel;
    @FXML private VBox productsListBox;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextArea  descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField oldPriceField;
    @FXML private TextField stockField;
    @FXML private TextField brandField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private HBox imagesBox;
    @FXML private Button addImageBtn;
    @FXML private Button saveProductBtn;
    @FXML private Button cancelEditBtn;

    @FXML private TextField supplierCountryField;
    @FXML private TextField deliveryDaysMinField;
    @FXML private TextField deliveryDaysMaxField;

    private final ProductService productService = new ProductService();

    private Product editingProduct = null;
    private final List<String> uploadedImageUrls = new ArrayList<>();
    private final List<String> categoryNames = new ArrayList<>();

    @FXML
    public void initialize() {
        loadCategories();
        loadProducts();
        loadStats();
        cancelEditBtn.setVisible(false);
    }

    private void loadStats() {
        var user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        AsyncTask.run(
                () -> productService.getSellerProducts(user.getId()),
                products -> {
                    totalProductsLabel.setText(String.valueOf(products.size()));
                    int totalSold = products.stream().mapToInt(Product::getSalesCount).sum();
                    totalSalesLabel.setText(String.valueOf(totalSold));
                },
                ex -> {}
        );
    }

    private void loadCategories() {
        AsyncTask.run(
                () -> productService.getCategories(),
                cats -> {
                    categoryNames.clear();
                    categoryNames.addAll(cats);
                    categoryCombo.getItems().setAll(cats);
                },
                ex -> {}
        );
    }

    private void loadProducts() {
        var user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        loadingIndicator.setVisible(true);
        productsListBox.getChildren().clear();
        AsyncTask.run(
                () -> productService.getSellerProducts(user.getId()),
                products -> {
                    loadingIndicator.setVisible(false);
                    for (Product p : products) {
                        productsListBox.getChildren().add(createProductRow(p));
                    }
                },
                ex -> loadingIndicator.setVisible(false)
        );
    }

    private HBox createProductRow(Product product) {
        HBox row = new HBox(12);
        row.getStyleClass().add("cart-item");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));

        ImageView iv = new ImageView(ImageLoader.load(product.getFirstImageUrl()));
        iv.setFitWidth(56);
        iv.setFitHeight(56);
        iv.setPreserveRatio(false);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(product.getName());
        name.getStyleClass().add("card-title");

        String articleStr = product.getArticle() != null ? " | Арт: " + product.getArticle() : "";
        int imgCount = product.getImageUrls() != null ? product.getImageUrls().size() : 0;
        String imgInfo = imgCount > 0 ? " | 🖼 " + imgCount : " | нет фото";
        Label price = new Label(FormatUtil.formatPrice(product.getPrice())
                + " | Склад: " + product.getStock() + articleStr + imgInfo);
        price.getStyleClass().add("label-muted");
        info.getChildren().addAll(name, price);

        Label statusLabel = new Label(product.isActive() ? "Активен" : "Скрыт");
        statusLabel.getStyleClass().addAll("status-badge",
                product.isActive() ? "status-delivered" : "status-cancelled");

        HBox btns = new HBox(6);
        btns.setAlignment(Pos.CENTER);

        Button toggleBtn = new Button(product.isActive() ? "Скрыть" : "Показать");
        toggleBtn.getStyleClass().add("btn-outline");
        toggleBtn.setOnAction(e -> toggleActive(product, !product.isActive()));

        Button editBtn = new Button("Изменить");
        editBtn.getStyleClass().add("btn-secondary");
        editBtn.setOnAction(e -> openEditDialog(product));

        Button delBtn = new Button("Удалить");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setOnAction(e -> deleteProduct(product));

        btns.getChildren().addAll(toggleBtn, editBtn, delBtn);
        row.getChildren().addAll(iv, info, statusLabel, btns);
        return row;
    }

    private void openEditDialog(Product product) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Редактировать товар: " + product.getName());
        dialog.setMinWidth(640);
        dialog.setMinHeight(660);

        TextField dNameField        = new TextField(product.getName());
        TextArea  dDescField        = new TextArea(product.getDescription() != null ? product.getDescription() : "");
        dDescField.setPrefRowCount(3);
        TextField dPriceField       = new TextField(product.getPrice() != null ? product.getPrice().toPlainString() : "");
        TextField dOldPriceField    = new TextField(product.getOldPrice() != null ? product.getOldPrice().toPlainString() : "");
        TextField dStockField       = new TextField(String.valueOf(product.getStock()));
        TextField dBrandField       = new TextField(product.getBrand() != null ? product.getBrand() : "");
        TextField dSupplierCountry  = new TextField(product.getSupplierCountry() != null ? product.getSupplierCountry() : "");
        dSupplierCountry.setPromptText("Например: Китай, Германия, Россия");
        TextField dDeliveryMin      = new TextField(product.getDeliveryDaysMin() != null ? product.getDeliveryDaysMin().toString() : "3");
        dDeliveryMin.setPromptText("3");
        TextField dDeliveryMax      = new TextField(product.getDeliveryDaysMax() != null ? product.getDeliveryDaysMax().toString() : "7");
        dDeliveryMax.setPromptText("7");

        ComboBox<String> dCatCombo  = new ComboBox<>();
        dCatCombo.getItems().setAll(categoryNames);
        dCatCombo.setMaxWidth(Double.MAX_VALUE);

        if (product.getCategory() != null && categoryNames.contains(product.getCategory())) {
            dCatCombo.setValue(product.getCategory());
        } else if (!categoryNames.isEmpty()) {
            AsyncTask.run(
                    () -> productService.getCategories(),
                    cats -> {
                        dCatCombo.getItems().setAll(cats);
                        if (product.getCategory() != null) dCatCombo.setValue(product.getCategory());
                    },
                    ex -> {}
            );
        }

        List<String> dialogImageUrls = new ArrayList<>();
        if (product.getImageUrls() != null) dialogImageUrls.addAll(product.getImageUrls());

        FlowPane imagesPane = new FlowPane(8, 8);
        imagesPane.setPrefWrapLength(560);
        imagesPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10;");
        imagesPane.setMinHeight(90);

        Label uploadingLabel = new Label();
        uploadingLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12;");
        uploadingLabel.setVisible(false);

        rebuildPreviews(dialogImageUrls, imagesPane, uploadingLabel, dialog, product);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.setStyle("-fx-background-color: #ffffff;");

        ColumnConstraints col1 = new ColumnConstraints(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);
        grid.getColumnConstraints().addAll(col1, col2);

        addRow(grid, 0,  "Название *",      dNameField);
        addRow(grid, 1,  "Описание",         dDescField);
        addRow(grid, 2,  "Цена *",           dPriceField);
        addRow(grid, 3,  "Старая цена",      dOldPriceField);
        addRow(grid, 4,  "Склад *",          dStockField);
        addRow(grid, 5,  "Бренд",            dBrandField);
        addRow(grid, 6,  "Категория",        dCatCombo);
        addRow(grid, 7,  "Страна поставщика", dSupplierCountry);

        HBox deliveryBox = new HBox(8);
        Label minLbl = new Label("от");
        minLbl.setStyle("-fx-text-fill: #6c757d;");
        Label maxLbl = new Label("до");
        maxLbl.setStyle("-fx-text-fill: #6c757d;");
        Label daysLbl = new Label("дн.");
        daysLbl.setStyle("-fx-text-fill: #6c757d;");
        dDeliveryMin.setPrefWidth(60);
        dDeliveryMax.setPrefWidth(60);
        deliveryBox.getChildren().addAll(minLbl, dDeliveryMin, maxLbl, dDeliveryMax, daysLbl);
        addRow(grid, 8, "Доставка (дней)", deliveryBox);

        Label imgLabel = new Label("Фотографии");
        imgLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
        grid.add(imgLabel,       0, 9);
        grid.add(imagesPane,     1, 9);
        grid.add(uploadingLabel, 1, 10);

        Button saveBtn   = new Button("Сохранить");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setDefaultButton(true);

        Button cancelBtn = new Button("Отмена");
        cancelBtn.getStyleClass().add("btn-outline");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox dialogBtns = new HBox(10, cancelBtn, saveBtn);
        dialogBtns.setAlignment(Pos.CENTER_RIGHT);
        dialogBtns.setPadding(new Insets(0, 16, 16, 16));

        saveBtn.setOnAction(e -> {
            String dName = dNameField.getText().trim();
            if (dName.isEmpty()) { showDialogError("Введите название товара"); return; }
            BigDecimal dPrice;
            int dStock;
            try {
                dPrice = new BigDecimal(dPriceField.getText().trim());
                dStock = Integer.parseInt(dStockField.getText().trim());
            } catch (NumberFormatException ex) {
                showDialogError("Неверный формат цены или количества"); return;
            }

            product.setName(dName);
            product.setDescription(dDescField.getText().trim());
            product.setPrice(dPrice);
            product.setStock(dStock);
            product.setBrand(dBrandField.getText().trim());
            product.setCategory(dCatCombo.getValue());
            product.setImageUrls(new ArrayList<>(dialogImageUrls));
            product.setSupplierCountry(dSupplierCountry.getText().trim());

            try {
                String minStr = dDeliveryMin.getText().trim();
                String maxStr = dDeliveryMax.getText().trim();
                if (!minStr.isEmpty()) product.setDeliveryDaysMin(Integer.parseInt(minStr));
                if (!maxStr.isEmpty()) product.setDeliveryDaysMax(Integer.parseInt(maxStr));
            } catch (NumberFormatException ignored) {}

            String oldP = dOldPriceField.getText().trim();
            if (!oldP.isEmpty()) {
                try { product.setOldPrice(new BigDecimal(oldP)); }
                catch (NumberFormatException ignored) {}
            } else {
                product.setOldPrice(null);
            }

            saveBtn.setDisable(true);
            AsyncTask.run(
                    () -> { productService.updateProduct(product.getId(), product); return null; },
                    v -> {
                        saveBtn.setDisable(false);
                        dialog.close();
                        loadProducts();
                        loadStats();
                        AlertUtil.showSuccess("Товар обновлён");
                    },
                    ex -> { saveBtn.setDisable(false); showDialogError(ex.getMessage()); }
            );
        });

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox root = new VBox(0, scroll, dialogBtns);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #ffffff;");

        Scene scene = new Scene(root, 660, 620);
        if (saveProductBtn.getScene() != null && saveProductBtn.getScene().getStylesheets() != null) {
            scene.getStylesheets().addAll(saveProductBtn.getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void rebuildPreviews(List<String> urls, FlowPane pane, Label uploadingLabel, Stage dialog, Product product) {
        pane.getChildren().clear();

        for (int i = 0; i < urls.size(); i++) {
            final String url = urls.get(i);
            final int    idx = i;

            VBox wrap = new VBox(4);
            wrap.setAlignment(Pos.TOP_CENTER);
            wrap.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;");

            ImageView iv = new ImageView(ImageLoader.load(url));
            iv.setFitWidth(84);
            iv.setFitHeight(84);
            iv.setPreserveRatio(false);

            Label numLabel = new Label(idx == 0 ? "Главная" : "#" + (idx + 1));
            numLabel.setStyle("-fx-font-size: 10; -fx-text-fill: " + (idx == 0 ? "#6366f1" : "#6c757d") + ";");

            HBox imgBtns = new HBox(3);
            imgBtns.setAlignment(Pos.CENTER);

            if (idx > 0) {
                Button leftBtn = new Button("←");
                styleIconBtn(leftBtn);
                leftBtn.setOnAction(e -> {
                    String moved = urls.remove(idx);
                    urls.add(idx - 1, moved);
                    rebuildPreviews(urls, pane, uploadingLabel, dialog, product);
                });
                imgBtns.getChildren().add(leftBtn);
            }

            Button delBtn = new Button("✕");
            delBtn.setStyle("-fx-font-size: 11; -fx-padding: 1 5; -fx-text-fill: #dc3545; -fx-cursor: hand;");
            delBtn.setTooltip(new Tooltip("Удалить фото"));
            delBtn.setOnAction(e -> {
                urls.remove(url);
                if (url.startsWith("http")) {
                    AsyncTask.run(
                            () -> { productService.deleteProductImage(url); return null; },
                            v  -> {},
                            ex -> {}
                    );
                }
                rebuildPreviews(urls, pane, uploadingLabel, dialog, product);
            });
            imgBtns.getChildren().add(delBtn);

            if (idx < urls.size() - 1) {
                Button rightBtn = new Button("→");
                styleIconBtn(rightBtn);
                rightBtn.setOnAction(e -> {
                    String moved = urls.remove(idx);
                    urls.add(idx + 1, moved);
                    rebuildPreviews(urls, pane, uploadingLabel, dialog, product);
                });
                imgBtns.getChildren().add(rightBtn);
            }

            wrap.getChildren().addAll(iv, numLabel, imgBtns);
            pane.getChildren().add(wrap);
        }

        if (urls.size() < 8) {
            Button addBtn = new Button("+ Фото");
            addBtn.setStyle(
                    "-fx-background-color: #f1f3f5; -fx-border-color: #ced4da; " +
                            "-fx-border-radius: 4; -fx-background-radius: 4; " +
                            "-fx-padding: 30 14; -fx-cursor: hand; -fx-font-size: 12;");
            addBtn.setOnAction(e -> pickAndUploadImage(product, urls, pane, uploadingLabel, dialog));
            pane.getChildren().add(addBtn);
        }
    }

    private void pickAndUploadImage(Product product, List<String> urls, FlowPane pane,
                                    Label uploadingLabel, Stage owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите изображение");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.webp"));
        File file = chooser.showOpenDialog(owner);
        if (file == null) return;

        uploadingLabel.setText("Загрузка " + file.getName() + "...");
        uploadingLabel.setVisible(true);

        pane.getChildren().stream()
                .filter(n -> n instanceof Button)
                .forEach(n -> n.setDisable(true));

        AsyncTask.run(
                () -> {
                    byte[] data = Files.readAllBytes(file.toPath());
                    String ext  = file.getName().contains(".") ?
                            file.getName().substring(file.getName().lastIndexOf('.')) : ".jpg";
                    String mime = ext.equalsIgnoreCase(".png") ? "image/png" : "image/jpeg";
                    return productService.uploadProductImage(product.getId(), data, file.getName(), mime);
                },
                url -> {
                    uploadingLabel.setVisible(false);
                    urls.add(url);
                    rebuildPreviews(urls, pane, uploadingLabel, owner, product);
                },
                ex -> {
                    uploadingLabel.setVisible(false);
                    pane.getChildren().stream()
                            .filter(n -> n instanceof Button)
                            .forEach(n -> n.setDisable(false));
                    showDialogError("Ошибка загрузки: " + ex.getMessage());
                }
        );
    }

    @FXML
    private void onCancelEdit() {
        editingProduct = null;
        clearForm();
        cancelEditBtn.setVisible(false);
        formTitleLabel.setText("Добавить товар");
    }

    @FXML
    private void onAddImage() {
        var user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите изображение");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.webp"));
        File file = chooser.showOpenDialog(addImageBtn.getScene().getWindow());
        if (file == null) return;

        addImageBtn.setDisable(true);
        AsyncTask.run(
                () -> {
                    byte[] data = Files.readAllBytes(file.toPath());
                    String mime = file.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                    String pid  = editingProduct != null ? editingProduct.getId() : user.getId();
                    return productService.uploadProductImage(pid, data, file.getName(), mime);
                },
                url -> { addImageBtn.setDisable(false); uploadedImageUrls.add(url); renderImagePreviews(); },
                ex  -> { addImageBtn.setDisable(false); AlertUtil.showError("Ошибка загрузки", ex.getMessage()); }
        );
    }

    private void renderImagePreviews() {
        imagesBox.getChildren().clear();
        for (String url : new ArrayList<>(uploadedImageUrls)) {
            VBox wrap = new VBox(2);
            wrap.setAlignment(Pos.TOP_CENTER);
            ImageView iv = new ImageView(ImageLoader.load(url));
            iv.setFitWidth(60); iv.setFitHeight(60); iv.setPreserveRatio(false);
            Button rem = new Button("✕");
            rem.getStyleClass().add("btn-icon");
            rem.setOnAction(e -> {
                uploadedImageUrls.remove(url);
                renderImagePreviews();
                if (url.startsWith("http")) {
                    AsyncTask.run(
                            () -> { productService.deleteProductImage(url); return null; },
                            v  -> {},
                            ex -> {}
                    );
                }
            });
            wrap.getChildren().addAll(iv, rem);
            imagesBox.getChildren().add(wrap);
        }
    }

    @FXML
    private void onSaveProduct() {
        var user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        String name = nameField.getText().trim();
        if (name.isEmpty()) { AlertUtil.showError("Ошибка", "Введите название товара"); return; }

        BigDecimal price;
        int stock;
        try {
            price = new BigDecimal(priceField.getText().trim());
            stock = Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.showError("Ошибка", "Неверный формат цены или количества"); return;
        }

        Product p = new Product();
        p.setSellerId(user.getId());
        p.setName(name);
        p.setDescription(descriptionField.getText().trim());
        p.setPrice(price);
        p.setStock(stock);
        p.setBrand(brandField.getText().trim());
        p.setImageUrls(new ArrayList<>(uploadedImageUrls));
        p.setCategory(categoryCombo.getValue());
        p.setSupplierCountry(supplierCountryField.getText().trim());

        try {
            String minStr = deliveryDaysMinField.getText().trim();
            String maxStr = deliveryDaysMaxField.getText().trim();
            p.setDeliveryDaysMin(!minStr.isEmpty() ? Integer.parseInt(minStr) : 3);
            p.setDeliveryDaysMax(!maxStr.isEmpty() ? Integer.parseInt(maxStr) : 7);
        } catch (NumberFormatException e) {
            p.setDeliveryDaysMin(3);
            p.setDeliveryDaysMax(7);
        }

        String oldPriceText = oldPriceField.getText().trim();
        if (!oldPriceText.isEmpty()) {
            try { p.setOldPrice(new BigDecimal(oldPriceText)); }
            catch (NumberFormatException ignored) {}
        }

        saveProductBtn.setDisable(true);
        AsyncTask.run(
                () -> productService.createProduct(p),
                saved -> { saveProductBtn.setDisable(false); afterSave(); AlertUtil.showSuccess("Товар добавлен!"); },
                ex   -> { saveProductBtn.setDisable(false); AlertUtil.showError("Ошибка", ex.getMessage()); }
        );
    }

    private void afterSave() {
        editingProduct = null;
        clearForm();
        cancelEditBtn.setVisible(false);
        formTitleLabel.setText("Добавить товар");
        loadProducts();
        loadStats();
    }

    private void toggleActive(Product product, boolean active) {
        AsyncTask.run(
                () -> { productService.setProductActive(product.getId(), active); return null; },
                v  -> { product.setActive(active); loadProducts(); },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage())
        );
    }

    private void deleteProduct(Product product) {
        if (!AlertUtil.showConfirm("Удалить товар?",
                "Товар \"" + product.getName() + "\" будет удалён безвозвратно")) return;
        AsyncTask.run(
                () -> { productService.deleteProduct(product.getId()); return null; },
                v  -> { loadProducts(); loadStats(); },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage())
        );
    }

    private void clearForm() {
        nameField.clear(); descriptionField.clear(); priceField.clear();
        oldPriceField.clear(); stockField.clear(); brandField.clear();
        supplierCountryField.clear();
        deliveryDaysMinField.clear(); deliveryDaysMaxField.clear();
        categoryCombo.setValue(null);
        uploadedImageUrls.clear();
        imagesBox.getChildren().clear();
        editingProduct = null;
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
        GridPane.setValignment(lbl, javafx.geometry.VPos.TOP);
        GridPane.setMargin(lbl, new Insets(4, 0, 0, 0));
        GridPane.setFillWidth(control, true);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        grid.add(lbl,     0, row);
        grid.add(control, 1, row);
    }

    private void styleIconBtn(Button btn) {
        btn.setStyle("-fx-font-size: 10; -fx-padding: 1 5; -fx-cursor: hand;");
    }

    private void showDialogError(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Ошибка");
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }
}
