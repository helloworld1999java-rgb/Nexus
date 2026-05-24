package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.model.Product;
import com.marketplace.model.PromoCode;
import com.marketplace.model.User;
import com.marketplace.service.AdminService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.PromoCodeAdminService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.util.List;

public class AdminController {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label totalRevenueLabel;

    @FXML
    private TextField userSearchField;
    @FXML
    private ComboBox<String> roleFilterCombo;
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TextField productSearchField;
    @FXML
    private ComboBox<String> productStatusFilter;
    @FXML
    private TableView<Product> productsTable;

    @FXML
    private ComboBox<String> orderStatusFilter;
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private Label ordersCountLabel;

    @FXML
    private TableView<PromoCode> promoTable;

    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final PromoCodeAdminService promoService = new PromoCodeAdminService();

    private List<User> allUsers;
    private List<Product> allProducts;
    private List<Order> allOrders;

    @FXML
    public void initialize() {
        roleFilterCombo.getItems().addAll("Все", "user", "seller", "admin");
        roleFilterCombo.setValue("Все");
        productStatusFilter.getItems().addAll("Все", "Активные", "Скрытые");
        productStatusFilter.setValue("Все");
        orderStatusFilter.getItems().addAll("Все", "created", "processing", "shipped", "delivered", "cancelled");
        orderStatusFilter.setValue("Все");

        setupUserTable();
        setupProductTable();
        setupOrderTable();
        setupPromoTable();

        loadStats();
        loadUsers();
        loadProducts();
        loadOrders();
        loadPromos();
    }

    private void loadStats() {
        AsyncTask.run(
                () -> adminService.getSalesStats(),
                stats -> {
                    totalUsersLabel.setText(String.valueOf(stats.get("totalUsers").getAsInt()));
                    totalProductsLabel.setText(String.valueOf(stats.get("totalProducts").getAsInt()));
                    totalOrdersLabel.setText(String.valueOf(stats.get("totalOrders").getAsInt()));
                    totalRevenueLabel.setText(
                            FormatUtil.formatPrice(BigDecimal.valueOf(stats.get("totalRevenue").getAsDouble())));
                },
                ex -> {
                }
        );
    }


    private void setupUserTable() {
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        emailCol.setPrefWidth(220);

        TableColumn<User, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFullName() != null ? d.getValue().getFullName() : ""));
        nameCol.setPrefWidth(180);

        TableColumn<User, String> roleCol = new TableColumn<>("Роль");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
        roleCol.setPrefWidth(100);

        TableColumn<User, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isBlocked() ? "Заблокирован" : "Активен"));
        statusCol.setPrefWidth(130);

        TableColumn<User, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(280);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button blockBtn = new Button();
            private final ComboBox<String> roleBox = new ComboBox<>();
            private final Button roleBtn = new Button("Сменить роль");
            private final HBox box = new HBox(6, blockBtn, roleBox, roleBtn);

            {
                roleBox.getItems().addAll("user", "seller", "admin");
                roleBox.setPrefWidth(90);
                blockBtn.getStyleClass().add("btn-outline");
                roleBtn.getStyleClass().add("btn-primary");
                blockBtn.setOnAction(e -> toggleUserBlock(getTableView().getItems().get(getIndex())));
                roleBtn.setOnAction(e -> {
                    String r = roleBox.getValue();
                    if (r != null) setUserRole(getTableView().getItems().get(getIndex()), r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                User u = getTableView().getItems().get(getIndex());
                blockBtn.setText(u.isBlocked() ? "Разблокировать" : "Заблокировать");
                roleBox.setValue(u.getRole());
                setGraphic(box);
            }
        });

        usersTable.getColumns().setAll(emailCol, nameCol, roleCol, statusCol, actionsCol);
    }

    private void loadUsers() {
        AsyncTask.run(() -> adminService.getAllUsers(),
                users -> {
                    allUsers = users;
                    usersTable.getItems().setAll(users);
                }, ex -> {
                });
    }

    @FXML
    private void onSearchUsers() {
        if (allUsers == null) return;
        String q = userSearchField.getText().toLowerCase().trim();
        String role = roleFilterCombo.getValue();
        var filtered = allUsers.stream()
                .filter(u -> q.isEmpty() || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q)))
                .filter(u -> role == null || role.equals("Все") || role.equals(u.getRole()))
                .toList();
        usersTable.getItems().setAll(filtered);
    }

    private void toggleUserBlock(User user) {
        boolean block = !user.isBlocked();
        AsyncTask.run(() -> {
                    adminService.blockUser(user.getId(), block);
                    return null;
                },
                v -> {
                    user.setBlocked(block);
                    usersTable.refresh();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    private void setUserRole(User user, String role) {
        AsyncTask.run(() -> {
                    adminService.setUserRole(user.getId(), role);
                    return null;
                },
                v -> {
                    user.setRole(role);
                    usersTable.refresh();
                    com.marketplace.config.AppState state = com.marketplace.config.AppState.getInstance();
                    if (state.getCurrentUser() != null && state.getCurrentUser().getId().equals(user.getId())) {
                        state.getCurrentUser().setRole(role);
                        MainController mc = MainController.getInstance();
                        if (mc != null) mc.updateNavVisibility();
                    }
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }


    private void setupProductTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<Product, String> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty(FormatUtil.formatPrice(d.getValue().getPrice())));
        priceCol.setPrefWidth(120);

        TableColumn<Product, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Активен" : "Скрыт"));
        statusCol.setPrefWidth(100);

        TableColumn<Product, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button delBtn = new Button("Удалить");
            private final HBox box = new HBox(6, toggleBtn, delBtn);

            {
                toggleBtn.getStyleClass().add("btn-outline");
                delBtn.getStyleClass().add("btn-danger");
                toggleBtn.setOnAction(e -> toggleProductActive(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deleteProduct(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                toggleBtn.setText(getTableView().getItems().get(getIndex()).isActive() ? "Скрыть" : "Показать");
                setGraphic(box);
            }
        });

        productsTable.getColumns().setAll(nameCol, priceCol, statusCol, actionsCol);
    }

    private void loadProducts() {
        AsyncTask.run(() -> productService.getProducts(null, null, "created_at", "desc", null, null, null, 200, 0),
                products -> {
                    allProducts = products;
                    productsTable.getItems().setAll(products);
                }, ex -> {
                });
    }

    @FXML
    private void onSearchProducts() {
        if (allProducts == null) return;
        String q = productSearchField.getText().toLowerCase().trim();
        String status = productStatusFilter.getValue();
        var filtered = allProducts.stream()
                .filter(p -> q.isEmpty() || p.getName().toLowerCase().contains(q))
                .filter(p -> status == null || status.equals("Все")
                        || (status.equals("Активные") && p.isActive()) || (status.equals("Скрытые") && !p.isActive()))
                .toList();
        productsTable.getItems().setAll(filtered);
    }

    private void toggleProductActive(Product product) {
        boolean active = !product.isActive();
        AsyncTask.run(() -> {
                    productService.setProductActive(product.getId(), active);
                    return null;
                },
                v -> {
                    product.setActive(active);
                    productsTable.refresh();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    private void deleteProduct(Product product) {
        if (!AlertUtil.showConfirm("Удалить товар?", product.getName())) return;
        AsyncTask.run(() -> {
                    productService.deleteProduct(product.getId());
                    return null;
                },
                v -> loadProducts(), ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }


    private void setupOrderTable() {
        TableColumn<Order, String> idCol = new TableColumn<>("№ Заказа");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getShortId()));
        idCol.setPrefWidth(120);

        TableColumn<Order, String> amountCol = new TableColumn<>("Сумма");
        amountCol.setCellValueFactory(d -> new SimpleStringProperty(FormatUtil.formatPrice(d.getValue().getTotalAmount())));
        amountCol.setPrefWidth(120);

        TableColumn<Order, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatusLabel()));
        statusCol.setPrefWidth(130);

        TableColumn<Order, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(FormatUtil.formatDate(d.getValue().getCreatedAt())));
        dateCol.setPrefWidth(120);

        TableColumn<Order, Void> actionsCol = new TableColumn<>("Изменить статус");
        actionsCol.setPrefWidth(230);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            private final Button applyBtn = new Button("Применить");
            private final HBox box = new HBox(6, statusCombo, applyBtn);

            {
                statusCombo.getItems().addAll("created", "processing", "shipped", "delivered", "cancelled");
                statusCombo.setPrefWidth(120);
                applyBtn.getStyleClass().add("btn-primary");
                applyBtn.setOnAction(e -> {
                    if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) return;
                    String ns = statusCombo.getValue();
                    if (ns != null) updateOrderStatus(getTableView().getItems().get(getIndex()), ns);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                statusCombo.setValue(getTableView().getItems().get(getIndex()).getStatus());
                setGraphic(box);
            }
        });

        ordersTable.getColumns().setAll(idCol, amountCol, statusCol, dateCol, actionsCol);
    }

    private void loadOrders() {
        AsyncTask.run(() -> orderService.getAllOrders(),
                orders -> {
                    allOrders = orders;
                    ordersTable.getItems().setAll(orders);
                    ordersCountLabel.setText("Всего: " + orders.size());
                },
                ex -> {
                });
    }

    @FXML
    private void onFilterOrders() {
        if (allOrders == null) return;
        String status = orderStatusFilter.getValue();
        var filtered = allOrders.stream()
                .filter(o -> status == null || status.equals("Все") || status.equals(o.getStatus())).toList();
        ordersTable.getItems().setAll(filtered);
        ordersCountLabel.setText("Показано: " + filtered.size());
    }

    private void updateOrderStatus(Order order, String newStatus) {
        AsyncTask.run(() -> {
                    orderService.updateOrderStatus(order.getId(), newStatus);
                    return null;
                },
                v -> {
                    order.setStatus(newStatus);
                    ordersTable.refresh();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }


    private void setupPromoTable() {
        TableColumn<PromoCode, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        codeCol.setPrefWidth(130);

        TableColumn<PromoCode, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(
                "percent".equals(d.getValue().getDiscountType()) ? "%" : "₽"));
        typeCol.setPrefWidth(60);

        TableColumn<PromoCode, String> valueCol = new TableColumn<>("Скидка");
        valueCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDiscountValue() != null ? d.getValue().getDiscountValue().toPlainString() : "0"));
        valueCol.setPrefWidth(80);

        TableColumn<PromoCode, String> usesCol = new TableColumn<>("Использований");
        usesCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUsedCount() + " / " + (d.getValue().getMaxUses() > 0 ? d.getValue().getMaxUses() : "∞")));
        usesCol.setPrefWidth(120);

        TableColumn<PromoCode, String> activeCol = new TableColumn<>("Статус");
        activeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Активен" : "Отключён"));
        activeCol.setPrefWidth(90);

        TableColumn<PromoCode, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setPrefWidth(230);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Изменить");
            private final Button toggleBtn = new Button();
            private final Button delBtn = new Button("Удалить");
            private final HBox box = new HBox(6, editBtn, toggleBtn, delBtn);

            {
                editBtn.getStyleClass().add("btn-outline");
                toggleBtn.getStyleClass().add("btn-outline");
                delBtn.getStyleClass().add("btn-danger");
                editBtn.setOnAction(e -> showPromoDialog(getTableView().getItems().get(getIndex())));
                toggleBtn.setOnAction(e -> togglePromo(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> deletePromo(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                toggleBtn.setText(getTableView().getItems().get(getIndex()).isActive() ? "Отключить" : "Включить");
                setGraphic(box);
            }
        });

        promoTable.getColumns().setAll(codeCol, typeCol, valueCol, usesCol, activeCol, actionsCol);
    }

    private void loadPromos() {
        AsyncTask.run(() -> promoService.getAllPromoCodes(),
                promos -> promoTable.getItems().setAll(promos),
                ex -> AlertUtil.showError("Ошибка загрузки промокодов", ex.getMessage()));
    }

    @FXML
    private void onAddPromo() {
        showPromoDialog(null);
    }

    private void showPromoDialog(PromoCode existing) {
        try {
            com.marketplace.util.DialogHelper.showPromoCode(existing, this::loadPromos);
        } catch (Exception e) {
            AlertUtil.showError("Ошибка", e.getMessage());
        }
    }

    private void togglePromo(PromoCode promo) {
        boolean next = !promo.isActive();
        AsyncTask.run(() -> {
                    promoService.toggleActive(promo.getId(), next);
                    return null;
                },
                v -> {
                    promo.setActive(next);
                    promoTable.refresh();
                },
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    private void deletePromo(PromoCode promo) {
        if (!AlertUtil.showConfirm("Удалить промокод?",
                "Промокод «" + promo.getCode() + "» будет удалён безвозвратно.")) return;
        AsyncTask.run(() -> {
                    promoService.deletePromoCode(promo.getId());
                    return null;
                },
                v -> loadPromos(), ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    @FXML
    private void onRefresh() {
        loadStats();
        loadUsers();
        loadProducts();
        loadOrders();
        loadPromos();
    }
}
