package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.model.Address;
import com.marketplace.model.PaymentCard;
import com.marketplace.model.User;
import com.marketplace.service.AddressService;
import com.marketplace.service.AuthService;
import com.marketplace.service.PaymentCardService;
import com.marketplace.service.AdminService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.DialogHelper;
import com.marketplace.util.ErrorUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ProfileController {

    @FXML private TextField     fullNameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private Button        saveProfileBtn;
    @FXML private Label         profileSaveStatus;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        changePasswordBtn;
    @FXML private Label         passwordStatus;

    @FXML private VBox addressesBox;
    @FXML private HBox cardsBox;

    @FXML private Label currentRoleLabel;
    @FXML private VBox  becomeSellerBox;
    @FXML private VBox  becomeBuyerBox;

    private final AuthService        authService    = new AuthService();
    private final AddressService     addressService = new AddressService();
    private final PaymentCardService cardService    = new PaymentCardService();
    private final AdminService       adminService   = new AdminService();

    @FXML
    public void initialize() {
        profileSaveStatus.setVisible(false);
        passwordStatus.setVisible(false);
        fillUserData();
        loadAddresses();
        loadCards();
        updateRoleTab();
    }

    private void updateRoleTab() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        String role = user.getRole();
        String roleDisplay = switch (role != null ? role : "") {
            case "seller" -> "🛒 Продавец";
            case "admin"  -> "🔑 Администратор";
            default       -> "👤 Покупатель";
        };
        currentRoleLabel.setText("Текущая роль: " + roleDisplay);

        boolean isBuyer  = !"seller".equals(role) && !"admin".equals(role);
        boolean isSeller = "seller".equals(role);
        becomeSellerBox.setVisible(isBuyer);   becomeSellerBox.setManaged(isBuyer);
        becomeBuyerBox.setVisible(isSeller);   becomeBuyerBox.setManaged(isSeller);
    }

    private void fillUserData() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
        emailField.setText(user.getEmail()    != null ? user.getEmail()    : "");
        phoneField.setText(user.getPhone()    != null ? user.getPhone()    : "");
    }

    @FXML
    private void onSaveProfile() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        saveProfileBtn.setDisable(true);
        String newName  = fullNameField.getText().trim();
        String newPhone = phoneField.getText().trim();
        AsyncTask.run(
                (AsyncTask.ThrowingRunnable) () ->
                        authService.updateUserProfile(user.getId(), newName, newPhone),
                () -> {
                    saveProfileBtn.setDisable(false);
                    user.setFullName(newName);
                    user.setPhone(newPhone);
                    showStatus(profileSaveStatus, "✓ Сохранено", true);
                },
                ex -> {
                    saveProfileBtn.setDisable(false);
                    showStatus(profileSaveStatus, "✗ " + ex.getMessage(), false);
                }
        );
    }

    @FXML
    private void onChangePassword() {
        String pass    = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        if (pass.isEmpty() || !pass.equals(confirm)) {
            showStatus(passwordStatus, "✗ Пароли не совпадают", false);
            return;
        }
        if (pass.length() < 6) {
            showStatus(passwordStatus, "✗ Минимум 6 символов", false);
            return;
        }
        changePasswordBtn.setDisable(true);
        AsyncTask.run(
                (AsyncTask.ThrowingRunnable) () -> authService.updatePassword(pass),
                () -> {
                    changePasswordBtn.setDisable(false);
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    showStatus(passwordStatus, "✓ Пароль изменён", true);
                },
                ex -> {
                    changePasswordBtn.setDisable(false);
                    showStatus(passwordStatus, "✗ " + ex.getMessage(), false);
                }
        );
    }


    private void loadAddresses() {
        AsyncTask.run(() -> addressService.getUserAddresses(), this::renderAddresses, ex -> {});
    }

    private void renderAddresses(List<Address> addresses) {
        addressesBox.getChildren().clear();
        for (Address addr : addresses) addressesBox.getChildren().add(createAddressTile(addr));
    }

    private HBox createAddressTile(Address addr) {
        HBox tile = new HBox(10);
        tile.getStyleClass().add(addr.isDefault() ? "address-card address-card-default" : "address-card");
        tile.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tile, Priority.ALWAYS);
        tile.setPadding(new Insets(12));

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        if (addr.getLabel() != null && !addr.getLabel().isEmpty()) {
            Label lbl = new Label(addr.getLabel());
            lbl.getStyleClass().add("card-title");
            info.getChildren().add(lbl);
        }
        Label full = new Label(addr.getFullAddress());
        full.getStyleClass().add("label-muted");
        full.setWrapText(true);
        info.getChildren().add(full);
        if (addr.isDefault()) {
            Label badge = new Label("По умолчанию");
            badge.getStyleClass().add("default-badge");
            info.getChildren().add(badge);
        }

        VBox btns = new VBox(4);
        btns.setAlignment(Pos.CENTER);
        Button edit = new Button("Изменить");
        edit.getStyleClass().add("btn-outline");
        edit.setOnAction(e -> DialogHelper.showEditAddress(addr, updated ->
                AsyncTask.run(
                        (AsyncTask.ThrowingRunnable) () -> addressService.updateAddress(addr.getId(), updated),
                        this::loadAddresses,
                        ex2 -> AlertUtil.showError("Ошибка", ex2.getMessage()))));
        btns.getChildren().add(edit);
        if (!addr.isDefault()) {
            Button setDef = new Button("Основной");
            setDef.getStyleClass().add("btn-outline");
            setDef.setOnAction(e -> setDefaultAddress(addr.getId()));
            btns.getChildren().add(setDef);
        }
        Button del = new Button("Удалить");
        del.getStyleClass().add("btn-danger");
        del.setOnAction(e -> deleteAddress(addr));
        btns.getChildren().add(del);

        tile.getChildren().addAll(info, btns);
        return tile;
    }

    private void setDefaultAddress(String id) {
        AsyncTask.run(
                (AsyncTask.ThrowingRunnable) () -> addressService.setDefault(id),
                this::loadAddresses,
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    private void deleteAddress(Address addr) {
        if (!AlertUtil.showConfirm("Удалить адрес?", addr.getFullAddress())) return;
        AsyncTask.run(
                (AsyncTask.ThrowingRunnable) () -> addressService.deleteAddress(addr.getId()),
                this::loadAddresses,
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    @FXML
    private void onAddAddress() {
        DialogHelper.showAddAddress(addr ->
                AsyncTask.run(
                        () -> addressService.createAddress(addr),
                        v  -> loadAddresses(),
                        ex -> AlertUtil.showError("Ошибка", ex.getMessage())));
    }


    private void loadCards() {
        AsyncTask.run(() -> cardService.getUserCards(), this::renderCards, ex -> {});
    }

    private void renderCards(List<PaymentCard> cards) {
        cardsBox.getChildren().clear();
        for (PaymentCard card : cards) cardsBox.getChildren().add(createCardTile(card));
    }

    private VBox createCardTile(PaymentCard card) {
        VBox tile = new VBox(6);
        tile.getStyleClass().add(card.isDefault()
                ? "payment-card-tile payment-card-default" : "payment-card-tile");
        tile.setPadding(new Insets(14));

        String typeIcon = switch (card.getCardType() != null ? card.getCardType() : "") {
            case "visa"       -> "💳 VISA";
            case "mastercard" -> "💳 Mastercard";
            case "mir"        -> "💳 Мир";
            default           -> "💳 Карта";
        };
        Label type = new Label(typeIcon);
        type.getStyleClass().add("card-title");
        Label num = new Label(card.getDisplayNumber());
        num.getStyleClass().add("label-muted");
        Label exp = new Label("До: " + card.getExpiryDisplay());
        exp.getStyleClass().add("label-muted");

        HBox btns = new HBox(6);
        if (!card.isDefault()) {
            Button setDef = new Button("Основная");
            setDef.getStyleClass().add("btn-outline");
            setDef.setOnAction(e -> setDefaultCard(card.getId()));
            btns.getChildren().add(setDef);
        } else {
            Label badge = new Label("По умолчанию");
            badge.getStyleClass().add("default-badge");
            btns.getChildren().add(badge);
        }
        Button del = new Button("Удалить");
        del.getStyleClass().add("btn-danger");
        del.setOnAction(e -> deleteCard(card));
        btns.getChildren().add(del);

        tile.getChildren().addAll(type, num, exp, btns);
        return tile;
    }

    private void setDefaultCard(String id) {
        AsyncTask.run(
                (AsyncTask.ThrowingRunnable) () -> cardService.setDefault(id),
                this::loadCards,
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }

    private void deleteCard(PaymentCard card) {
        if (!AlertUtil.showConfirm("Удалить карту?", card.getDisplayNumber())) return;
        AsyncTask.run(
                (AsyncTask.ThrowingRunnable) () -> cardService.deleteCard(card.getId()),
                this::loadCards,
                ex -> AlertUtil.showError("Ошибка", ex.getMessage()));
    }


    @FXML
    private void onAddCard() {
        DialogHelper.showAddCard((card, fullCardNumber) ->
                AsyncTask.run(
                        () -> cardService.addCard(
                                fullCardNumber,
                                card.getCardHolder(),
                                card.getExpiryMonth(),
                                card.getExpiryYear()),
                        v  -> loadCards(),
                        ex -> AlertUtil.showError("Ошибка", ex.getMessage())));
    }

    @FXML
    private void onBecomeSeller() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        if ("seller".equals(user.getRole()) || "admin".equals(user.getRole())) {
            AlertUtil.showError("Информация", "Вы уже являетесь продавцом или администратором.");
            return;
        }
        if (!AlertUtil.showConfirm("Стать продавцом?",
                "Вы получите доступ к панели продавца и сможете добавлять свои товары на маркетплейс."))
            return;
        applyRoleChange(user, "seller",
                "🎉 Поздравляем!",
                "Вы теперь продавец! Раздел «Продавец» появился в навигации.");
    }

    @FXML
    private void onBecomeBuyer() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) return;
        if (!"seller".equals(user.getRole())) {
            AlertUtil.showError("Информация", "Эта опция доступна только продавцам.");
            return;
        }
        if (!AlertUtil.showConfirm("Вернуться к роли покупателя?",
                "Ваши товары останутся в базе, но будут скрыты. Вы сможете снова стать продавцом в любое время."))
            return;
        applyRoleChange(user, "user",
                "Готово",
                "Вы вернулись к роли покупателя. Раздел «Продавец» скрыт.");
    }

    private void applyRoleChange(User user, String newRole, String title, String message) {
        AsyncTask.run(
            () -> {
                adminService.setUserRole(user.getId(), newRole);
                return authService.getUserById(user.getId());
            },
            updatedUser -> {
                User effective = updatedUser != null ? updatedUser : user;
                if (updatedUser == null) effective.setRole(newRole);
                AppState.getInstance().setCurrentUser(effective);
                updateRoleTab();
                MainController mc = MainController.getInstance();
                if (mc != null) mc.updateNavVisibility();
                AlertUtil.showInfo(title, message);
            },
            ex -> AlertUtil.showError("Ошибка", ErrorUtil.friendlyMessage(ex))
        );
    }

    private void showStatus(Label label, String msg, boolean ok) {
        label.setText(msg);
        label.setStyle(ok ? "-fx-text-fill: #4cd97b;" : "-fx-text-fill: #e8445a;");
        label.setVisible(true);
    }
}