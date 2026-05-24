package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.CartItem;
import com.marketplace.service.CartService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import com.marketplace.util.ImageLoader;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartController {

    @FXML private VBox cartItemsBox;
    @FXML private Label totalLabel;
    @FXML private Label itemCountLabel;
    @FXML private Button checkoutBtn;
    @FXML private Button checkoutSelectedBtn;
    @FXML private Button clearCartBtn;
    @FXML private Label emptyLabel;
    @FXML private CheckBox selectAllCheckbox;
    @FXML private Label selectedSummaryLabel;

    private final CartService cartService = new CartService();
    private List<CartItem> items;

    private final Map<String, CheckBox> itemCheckboxes = new HashMap<>();

    @FXML
    public void initialize() {
        if (AppState.getInstance().getCurrentUser() == null) { showEmpty(true); return; }
        loadCart();
    }

    private void loadCart() {
        AsyncTask.run(
                () -> cartService.getCartItems(),
                cartItems -> { this.items = cartItems; renderCart(); },
                ex -> showEmpty(true)
        );
    }

    private void renderCart() {
        cartItemsBox.getChildren().clear();
        itemCheckboxes.clear();

        if (items == null || items.isEmpty()) { showEmpty(true); return; }
        showEmpty(false);

        for (CartItem item : items) {
            cartItemsBox.getChildren().add(createItemRow(item));
        }

        recalcTotal();
        updateSelectionUI();
    }

    private HBox createItemRow(CartItem item) {
        HBox row = new HBox(14);
        row.getStyleClass().add("cart-item");
        row.setAlignment(Pos.CENTER_LEFT);

        CheckBox cb = new CheckBox();
        cb.setSelected(true);
        cb.setStyle("-fx-cursor: hand; -fx-scale-x: 1.2; -fx-scale-y: 1.2;");
        cb.selectedProperty().addListener((obs, was, now) -> updateSelectionUI());
        itemCheckboxes.put(item.getId(), cb);

        String imgUrl = item.getProduct() != null ? item.getProduct().getFirstImageUrl() : null;
        ImageView iv = new ImageView(ImageLoader.load(imgUrl));
        iv.setFitWidth(72);
        iv.setFitHeight(72);
        iv.setPreserveRatio(false);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLabel = new Label(item.getProduct() != null ? item.getProduct().getName() : "Товар");
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true);
        Label priceLabel = new Label(
                item.getProduct() != null ? FormatUtil.formatPrice(item.getProduct().getPrice()) : "");
        priceLabel.getStyleClass().add("price-label");
        info.getChildren().addAll(nameLabel, priceLabel);

        HBox qtyBox = new HBox(0);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setStyle(
                "-fx-background-color: #f1f5f9; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 10px; " +
                "-fx-border-width: 1px;"
        );

        Button minus = new Button("−");
        minus.setStyle(
                "-fx-background-color: transparent; -fx-border-width: 0; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-text-fill: #475569; -fx-cursor: hand; " +
                "-fx-padding: 6 14 6 14; -fx-min-width: 36px;"
        );

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-text-fill: #0f172a; " +
                "-fx-min-width: 32px; -fx-alignment: center; -fx-padding: 0 4 0 4;"
        );
        qtyLabel.setAlignment(Pos.CENTER);

        Button plus = new Button("+");
        plus.setStyle(
                "-fx-background-color: transparent; -fx-border-width: 0; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-text-fill: #475569; -fx-cursor: hand; " +
                "-fx-padding: 6 14 6 14; -fx-min-width: 36px;"
        );

        minus.setOnMouseEntered(e -> minus.setStyle(minus.getStyle().replace("#475569", "#6366f1")));
        minus.setOnMouseExited(e  -> minus.setStyle(minus.getStyle().replace("#6366f1", "#475569")));
        plus.setOnMouseEntered(e  -> plus.setStyle(plus.getStyle().replace("#475569", "#6366f1")));
        plus.setOnMouseExited(e   -> plus.setStyle(plus.getStyle().replace("#6366f1", "#475569")));

        minus.setOnAction(e -> updateQty(item, item.getQuantity() - 1, qtyLabel));
        plus.setOnAction(e  -> updateQty(item, item.getQuantity() + 1, qtyLabel));
        qtyBox.getChildren().addAll(minus, qtyLabel, plus);

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("btn-danger");
        removeBtn.setOnAction(e -> removeItem(item));

        row.getChildren().addAll(cb, iv, info, qtyBox, removeBtn);
        return row;
    }

    private void updateQty(CartItem item, int newQty, Label qtyLabel) {
        if (newQty <= 0) { removeItem(item); return; }
        AsyncTask.run(
                () -> { cartService.updateQuantity(item.getId(), newQty); return null; },
                v -> {
                    item.setQuantity(newQty);
                    qtyLabel.setText(String.valueOf(newQty));
                    recalcTotal();
                    updateSelectionUI();
                    MainController.getInstance().updateCartBadge();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage())
        );
    }

    private void removeItem(CartItem item) {
        AsyncTask.run(
                () -> { cartService.removeFromCart(item.getId()); return null; },
                v -> {
                    itemCheckboxes.remove(item.getId());
                    items.remove(item);
                    renderCart();
                    MainController.getInstance().updateCartBadge();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage())
        );
    }

    private void recalcTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (CartItem item : items) total = total.add(item.getTotalPrice());
        }
        totalLabel.setText(FormatUtil.formatPrice(total));
        itemCountLabel.setText((items != null ? items.size() : 0) + " товар(а)");
    }

    /** Обновляет информацию о выбранных товарах и состояние кнопок */
    private void updateSelectionUI() {
        if (items == null) return;

        List<CartItem> selected = getSelectedItems();
        BigDecimal selectedTotal = selected.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int selCount = selected.size();
        int totalCount = items.size();

        if (selectedSummaryLabel != null) {
            if (selCount == 0) {
                selectedSummaryLabel.setText("Ничего не выбрано");
                selectedSummaryLabel.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 13px;");
            } else if (selCount == totalCount) {
                selectedSummaryLabel.setText("Выбрано всё · " + FormatUtil.formatPrice(selectedTotal));
                selectedSummaryLabel.setStyle("-fx-text-fill: -color-primary; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                selectedSummaryLabel.setText("Выбрано " + selCount + " из " + totalCount + " · " + FormatUtil.formatPrice(selectedTotal));
                selectedSummaryLabel.setStyle("-fx-text-fill: -color-primary; -fx-font-size: 13px; -fx-font-weight: bold;");
            }
        }

        if (selectAllCheckbox != null) {
            selectAllCheckbox.setIndeterminate(selCount > 0 && selCount < totalCount);
            if (!selectAllCheckbox.isIndeterminate()) {
                selectAllCheckbox.setSelected(selCount == totalCount && totalCount > 0);
            }
        }

        if (checkoutSelectedBtn != null) checkoutSelectedBtn.setDisable(selCount == 0);
    }

    private List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        if (items == null) return selected;
        for (CartItem item : items) {
            CheckBox cb = itemCheckboxes.get(item.getId());
            if (cb != null && cb.isSelected()) selected.add(item);
        }
        return selected;
    }

    @FXML
    private void onSelectAll() {
        if (selectAllCheckbox == null || items == null) return;
        boolean selectAll = selectAllCheckbox.isSelected();
        for (CheckBox cb : itemCheckboxes.values()) cb.setSelected(selectAll);
        updateSelectionUI();
    }

    @FXML
    private void onClearCart() {
        if (!AlertUtil.showConfirm("Очистить корзину?", "Все товары будут удалены")) return;
        AsyncTask.run(
                () -> { cartService.clearCart(); return null; },
                v -> {
                    items.clear();
                    itemCheckboxes.clear();
                    renderCart();
                    MainController.getInstance().updateCartBadge();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage())
        );
    }

    /** Оформить ВСЕ товары в корзине */
    @FXML
    private void onCheckout() {
        if (items == null || items.isEmpty()) return;
        AppState.getInstance().setSelectedCartItems(new ArrayList<>(items));
        MainController.getInstance().loadContent("checkout");
    }

    /** Оформить только ВЫБРАННЫЕ товары */
    @FXML
    private void onCheckoutSelected() {
        List<CartItem> selected = getSelectedItems();
        if (selected.isEmpty()) {
            AlertUtil.showError("Ничего не выбрано", "Отметьте хотя бы один товар для оформления");
            return;
        }
        AppState.getInstance().setSelectedCartItems(selected);
        MainController.getInstance().loadContent("checkout");
    }

    private void showEmpty(boolean empty) {
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
        cartItemsBox.setVisible(!empty);
        cartItemsBox.setManaged(!empty);
        if (checkoutBtn != null) checkoutBtn.setDisable(empty);
        if (checkoutSelectedBtn != null) checkoutSelectedBtn.setDisable(empty);
        if (clearCartBtn != null) clearCartBtn.setDisable(empty);
        if (selectAllCheckbox != null) { selectAllCheckbox.setVisible(!empty); selectAllCheckbox.setManaged(!empty); }
        if (selectedSummaryLabel != null) { selectedSummaryLabel.setVisible(!empty); selectedSummaryLabel.setManaged(!empty); }
    }
}
