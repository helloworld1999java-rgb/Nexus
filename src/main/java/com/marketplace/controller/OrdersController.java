package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.Order;
import com.marketplace.model.OrderItem;
import com.marketplace.service.CartService;
import com.marketplace.service.OrderService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.ErrorUtil;
import com.marketplace.util.FormatUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class OrdersController {

    @FXML private VBox ordersBox;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label emptyLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final OrderService orderService = new OrderService();
    private final CartService  cartService  = new CartService();

    @FXML
    public void initialize() {
        statusFilter.getItems().addAll("Все", "Создан", "В обработке", "Отправлен", "Доставлен", "Отменён");
        statusFilter.setValue("Все");
        statusFilter.setOnAction(e -> loadOrders());
        loadOrders();
    }

    private void loadOrders() {
        if (AppState.getInstance().getCurrentUser() == null) return;
        loadingIndicator.setVisible(true);
        ordersBox.getChildren().clear();

        AsyncTask.run(
            () -> orderService.getUserOrders(),
            orders -> {
                loadingIndicator.setVisible(false);
                String filter = statusFilter.getValue();
                List<Order> filtered = orders.stream()
                    .filter(o -> filter == null || filter.equals("Все")
                        || mapStatus(filter).equals(o.getStatus()))
                    .toList();
                emptyLabel.setVisible(filtered.isEmpty());
                emptyLabel.setManaged(filtered.isEmpty());
                for (Order order : filtered) ordersBox.getChildren().add(createOrderCard(order));
            },
            ex -> loadingIndicator.setVisible(false)
        );
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.getStyleClass().add("order-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Заказ " + order.getShortId());
        idLabel.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(order.getStatusLabel());
        statusBadge.getStyleClass().addAll("status-badge", order.getStatusStyle());

        Label dateLabel = new Label(FormatUtil.formatDate(order.getCreatedAt()));
        dateLabel.getStyleClass().add("label-muted");

        header.getChildren().addAll(idLabel, spacer, statusBadge, dateLabel);

        String deliveryText = order.getDeliveryStatusText();
        if (deliveryText != null) {
            Label deliveryLabel = new Label(deliveryText);
            deliveryLabel.setStyle(order.getDeliveryStatusStyle() +
                    " -fx-font-size: 13px; -fx-padding: 4 10; " +
                    "-fx-background-color: #f0f9ff; -fx-background-radius: 6;");
            card.getChildren().addAll(header, deliveryLabel);
        } else {
            card.getChildren().add(header);
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            VBox itemsList = new VBox(3);
            int shown = 0;
            for (OrderItem item : order.getItems()) {
                if (shown >= 3) {
                    int rem = order.getItems().size() - 3;
                    if (rem > 0) {
                        Label more = new Label("... ещё " + rem + " товар(а)");
                        more.getStyleClass().add("label-muted");
                        itemsList.getChildren().add(more);
                    }
                    break;
                }
                Label lbl = new Label("• " + item.getProductName() + " × " + item.getQuantity()
                    + "   " + FormatUtil.formatPrice(item.getTotalPrice()));
                lbl.getStyleClass().add("label-muted");
                itemsList.getChildren().add(lbl);
                shown++;
            }
            card.getChildren().add(itemsList);
        }

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label total = new Label("Итого: " + FormatUtil.formatPrice(order.getTotalAmount()));
        total.getStyleClass().add("price-label");
        total.setStyle("-fx-font-size: 16px;");

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        HBox actions = new HBox(8);
        if ("created".equals(order.getStatus()) || "processing".equals(order.getStatus())) {
            Button cancelBtn = new Button("Отменить");
            cancelBtn.getStyleClass().add("btn-danger");
            cancelBtn.setOnAction(e -> cancelOrder(order));
            actions.getChildren().add(cancelBtn);
        }
        Button repeatBtn = new Button("Повторить");
        repeatBtn.getStyleClass().add("btn-outline");
        repeatBtn.setOnAction(e -> repeatOrder(order));
        actions.getChildren().add(repeatBtn);

        footer.getChildren().addAll(total, fSpacer, actions);
        card.getChildren().add(footer);
        return card;
    }

    private void cancelOrder(Order order) {
        if (!AlertUtil.showConfirm("Отменить заказ?", "Заказ " + order.getShortId() + " будет отменён")) return;
        AsyncTask.run(
            () -> { orderService.cancelOrder(order.getId()); return null; },
            v  -> loadOrders(),
            ex -> AlertUtil.showError("Ошибка отмены", ErrorUtil.friendlyMessage(ex))
        );
    }

    private void repeatOrder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            AsyncTask.run(
                () -> orderService.getOrderById(order.getId()),
                fullOrder -> repeatOrder(fullOrder),
                ex -> AlertUtil.showError("Ошибка", ErrorUtil.friendlyMessage(ex))
            );
            return;
        }
        AsyncTask.run(
            () -> {
                int added = 0;
                for (OrderItem item : order.getItems()) {
                    if (item.getProductId() != null) {
                        cartService.addToCart(item.getProductId(), item.getQuantity());
                        added++;
                    }
                }
                return added;
            },
            added -> {
                if (added == 0) {
                    AlertUtil.showError("Корзина", "Не удалось добавить товары: товары из этого заказа больше не доступны.");
                    return;
                }
                AlertUtil.showInfo("Корзина", "Товары добавлены в корзину (" + added + " позиций)");
                MainController.getInstance().updateCartBadge();
                MainController.getInstance().loadContent("cart");
            },
            ex -> AlertUtil.showError("Ошибка", ErrorUtil.friendlyMessage(ex))
        );
    }

    private String mapStatus(String label) {
        return switch (label) {
            case "Создан"      -> "created";
            case "В обработке" -> "processing";
            case "Отправлен"   -> "shipped";
            case "Доставлен"   -> "delivered";
            case "Отменён"     -> "cancelled";
            default            -> "";
        };
    }
}
