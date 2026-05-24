package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.*;
import com.marketplace.service.*;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CheckoutController {

    @FXML private ComboBox<Address>     addressCombo;
    @FXML private ComboBox<PaymentCard> cardCombo;
    @FXML private TextField             promoField;
    @FXML private Button                applyPromoBtn;
    @FXML private Label                 promoResultLabel;
    @FXML private Label                 subtotalLabel;
    @FXML private Label                 discountLabel;
    @FXML private Label                 totalLabel;
    @FXML private Button                placeOrderBtn;
    @FXML private VBox                  orderItemsBox;
    @FXML private Label                 deliveryDateLabel;
    @FXML private VBox                  deliveryBlock;

    private final CartService        cartService    = new CartService();
    private final OrderService       orderService   = new OrderService();
    private final AddressService     addressService = new AddressService();
    private final PaymentCardService cardService    = new PaymentCardService();
    private final PromoCodeService   promoService   = new PromoCodeService();

    private List<CartItem> cartItems;
    private PromoCode      appliedPromo;
    private BigDecimal     subtotal = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        promoResultLabel.setVisible(false);

        addressCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Address a)   { return a == null ? "" : a.getFullAddress(); }
            public Address fromString(String s) { return null; }
        });
        cardCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(PaymentCard c)   { return c == null ? "" : c.getDisplayNumber(); }
            public PaymentCard fromString(String s) { return null; }
        });

        loadData();
    }

    private void loadData() {
        List<CartItem> selected = AppState.getInstance().getSelectedCartItems();
        if (selected != null && !selected.isEmpty()) {
            this.cartItems = selected;
            renderItems();
            calcSubtotal();
            updateDeliveryDate();
        } else {
            AsyncTask.run(
                () -> cartService.getCartItems(),
                items -> {
                    this.cartItems = items;
                    renderItems();
                    calcSubtotal();
                    updateDeliveryDate();
                },
                ex -> {}
            );
        }
        AsyncTask.run(
            () -> addressService.getUserAddresses(),
            addresses -> {
                addressCombo.getItems().setAll(addresses);
                addresses.stream().filter(Address::isDefault).findFirst().ifPresent(addressCombo::setValue);
            },
            ex -> {}
        );
        AsyncTask.run(
            () -> cardService.getUserCards(),
            cards -> {
                cardCombo.getItems().setAll(cards);
                cards.stream().filter(PaymentCard::isDefault).findFirst().ifPresent(cardCombo::setValue);
            },
            ex -> {}
        );
    }

    private void renderItems() {
        orderItemsBox.getChildren().clear();
        for (CartItem item : cartItems) {
            if (item.getProduct() == null) continue;
            Label lbl = new Label(item.getProduct().getName()
                + " × " + item.getQuantity()
                + "   " + FormatUtil.formatPrice(item.getTotalPrice()));
            lbl.getStyleClass().add("label-muted");
            orderItemsBox.getChildren().add(lbl);
        }
    }

    private void updateDeliveryDate() {
        if (cartItems == null || cartItems.isEmpty()) {
            deliveryDateLabel.setText("Не определена");
            return;
        }

        int maxDays = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct() == null) continue;
            int days = item.getProduct().getDeliveryDaysMax() != null
                    ? item.getProduct().getDeliveryDaysMax() : 7;
            if (days > maxDays) maxDays = days;
        }
        if (maxDays == 0) maxDays = 7;

        LocalDate deliveryDate = LocalDate.now().plusDays(maxDays);
        DateTimeFormatter fmt  = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));
        deliveryDateLabel.setText(deliveryDate.format(fmt));
    }

    private void calcSubtotal() {
        subtotal = cartItems.stream().map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        subtotalLabel.setText(FormatUtil.formatPrice(subtotal));
        recalcTotal();
    }

    private void recalcTotal() {
        BigDecimal discount = appliedPromo != null ? appliedPromo.calculateDiscount(subtotal) : BigDecimal.ZERO;
        discountLabel.setText("−" + FormatUtil.formatPrice(discount));
        totalLabel.setText(FormatUtil.formatPrice(subtotal.subtract(discount).max(BigDecimal.ZERO)));
    }

    @FXML
    private void onApplyPromo() {
        String code = promoField.getText().trim().toUpperCase();
        if (code.isEmpty()) return;
        applyPromoBtn.setDisable(true);
        AsyncTask.run(
            () -> promoService.validatePromoCode(code, subtotal),
            promo -> {
                appliedPromo = promo;
                applyPromoBtn.setDisable(false);
                promoResultLabel.setText("✓ Скидка: −" + FormatUtil.formatPrice(promo.calculateDiscount(subtotal)));
                promoResultLabel.setStyle("-fx-text-fill: -color-success;");
                promoResultLabel.setVisible(true);
                recalcTotal();
            },
            ex -> {
                appliedPromo = null;
                applyPromoBtn.setDisable(false);
                promoResultLabel.setText("✗ " + ex.getMessage());
                promoResultLabel.setStyle("-fx-text-fill: -color-danger;");
                promoResultLabel.setVisible(true);
                recalcTotal();
            }
        );
    }

    @FXML
    private void onPlaceOrder() {
        Address addr = addressCombo.getValue();
        PaymentCard card = cardCombo.getValue();
        if (addr == null) { AlertUtil.showError("Ошибка", "Выберите адрес доставки"); return; }
        if (card == null) { AlertUtil.showError("Ошибка", "Выберите способ оплаты"); return; }
        if (cartItems == null || cartItems.isEmpty()) { AlertUtil.showError("Ошибка", "Корзина пуста"); return; }

        placeOrderBtn.setDisable(true);
        BigDecimal discount = appliedPromo != null ? appliedPromo.calculateDiscount(subtotal) : BigDecimal.ZERO;
        BigDecimal total    = subtotal.subtract(discount).max(BigDecimal.ZERO);
        String promoCode    = appliedPromo != null ? appliedPromo.getCode() : null;

        AsyncTask.run(
            () -> orderService.createOrder(addr.getId(), card.getId(), promoCode, discount, total, cartItems),
            order -> {
                placeOrderBtn.setDisable(false);
                AppState.getInstance().setSelectedCartItems(null);
                if (appliedPromo != null) {
                    final String pid = appliedPromo.getId();
                    AsyncTask.run(() -> { promoService.incrementUsage(pid); return null; }, v -> {}, ex -> {});
                }
                AlertUtil.showInfo("Заказ оформлен",
                        "Ваш заказ " + order.getShortId() + " успешно оформлен!\n" +
                        "Ожидаемая доставка: " + deliveryDateLabel.getText());
                MainController.getInstance().updateCartBadge();
                MainController.getInstance().loadContent("orders");
            },
            ex -> { placeOrderBtn.setDisable(false); AlertUtil.showError("Ошибка", ex.getMessage()); }
        );
    }

    @FXML
    private void onBack() { MainController.getInstance().loadContent("cart"); }
}
