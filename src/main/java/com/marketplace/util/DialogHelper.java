package com.marketplace.util;

import com.marketplace.controller.dialog.AddAddressDialogController;
import com.marketplace.controller.dialog.AddCardDialogController;
import com.marketplace.model.Address;
import com.marketplace.model.PaymentCard;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.function.BiConsumer;

import java.util.function.Consumer;

public class DialogHelper {

    private static final String CSS_PATH  = "/com/marketplace/css/dialogs.css";
    private static final String CARD_FXML = "/com/marketplace/dialogs/add-card-dialog.fxml";
    private static final String ADDR_FXML = "/com/marketplace/dialogs/add-address-dialog.fxml";

    public static void showAddCard(BiConsumer<PaymentCard, String> onSave) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DialogHelper.class.getResource(CARD_FXML));
            Parent root = loader.load();

            AddCardDialogController ctrl = loader.getController();
            Stage stage = buildStage(root, "Добавить карту");

            ctrl.setOnSave((card, fullPan) -> {
                stage.close();
                if (onSave != null) onSave.accept(card, fullPan);
            });
            ctrl.setOnCancel(stage::close);

            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось открыть диалог карты", e);
        }
    }

    public static void showAddAddress(Consumer<Address> onSave) {
        showAddressDialog(null, onSave);
    }

    public static void showEditAddress(Address existing, Consumer<Address> onSave) {
        showAddressDialog(existing, onSave);
    }

    private static void showAddressDialog(Address existing, Consumer<Address> onSave) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DialogHelper.class.getResource(ADDR_FXML));
            Parent root = loader.load();

            AddAddressDialogController ctrl = loader.getController();
            Stage stage = buildStage(root, existing == null ? "Новый адрес" : "Редактировать адрес");

            if (existing != null) ctrl.prefill(existing);

            ctrl.setOnSave(addr -> {
                stage.close();
                if (onSave != null) onSave.accept(addr);
            });
            ctrl.setOnCancel(stage::close);

            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось открыть диалог адреса", e);
        }
    }

    public static void showPromoCode(com.marketplace.model.PromoCode existing, Runnable onSaved) {
        boolean isEdit = existing != null;

        javafx.scene.control.Label iconLbl = new javafx.scene.control.Label("🏷️");
        iconLbl.getStyleClass().add("dialog-icon");

        javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(
            isEdit ? "Редактировать промокод" : "Новый промокод");
        titleLbl.getStyleClass().add("dialog-title");

        javafx.scene.control.Label subLbl = new javafx.scene.control.Label("Заполните данные промокода");
        subLbl.getStyleClass().add("dialog-subtitle");

        javafx.scene.layout.VBox titleBox = new javafx.scene.layout.VBox(2, titleLbl, subLbl);
        javafx.scene.layout.HBox headerInner = new javafx.scene.layout.HBox(10, iconLbl, titleBox);
        headerInner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(headerInner);
        header.getStyleClass().add("dialog-header");

        javafx.scene.control.TextField codeField = styledField("Например: SALE20");
        javafx.scene.control.Label codeError = errorLabel();
        javafx.scene.layout.VBox codeBlock = fieldBlock("КОД ПРОМОКОДА", "🔖", codeField, codeError);

        javafx.scene.control.ComboBox<String> typeBox = new javafx.scene.control.ComboBox<>();
        typeBox.getItems().addAll("percent", "fixed");
        typeBox.setValue(isEdit && existing.getDiscountType() != null ? existing.getDiscountType() : "percent");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.HBox.setHgrow(typeBox, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox typeBlock = new javafx.scene.layout.VBox(4,
            fieldLabel("ТИП СКИДКИ"), typeBox);
        javafx.scene.layout.HBox.setHgrow(typeBlock, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.TextField valueField = styledField("0");
        javafx.scene.control.Label valueError = errorLabel();
        javafx.scene.layout.VBox valueBlock = fieldBlock("РАЗМЕР СКИДКИ", "%", valueField, valueError);
        javafx.scene.layout.HBox.setHgrow(valueBlock, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.HBox typeValueRow = new javafx.scene.layout.HBox(12, typeBlock, valueBlock);

        javafx.scene.control.TextField minField = styledField("0 — без ограничений");
        javafx.scene.control.Label minError = errorLabel();
        javafx.scene.layout.VBox minBlock = fieldBlock("МИН. СУММА ЗАКАЗА (₽)", "₽", minField, minError);
        javafx.scene.layout.HBox.setHgrow(minBlock, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.TextField maxField = styledField("0 — без ограничений");
        javafx.scene.control.Label maxError = errorLabel();
        javafx.scene.layout.VBox maxBlock = fieldBlock("МАКС. ИСПОЛЬЗОВАНИЙ", "∞", maxField, maxError);
        javafx.scene.layout.HBox.setHgrow(maxBlock, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.HBox minMaxRow = new javafx.scene.layout.HBox(12, minBlock, maxBlock);

        javafx.scene.control.TextField validField = styledField("ГГГГ-ММ-ДД");
        javafx.scene.control.Label validError = errorLabel();
        javafx.scene.layout.VBox validBlock = fieldBlock("СРОК ДЕЙСТВИЯ (необязательно)", "📅", validField, validError);

        javafx.scene.control.CheckBox activeBox = new javafx.scene.control.CheckBox();
        activeBox.getStyleClass().add("styled-checkbox");
        activeBox.setSelected(true);
        javafx.scene.control.Label activeLbl = new javafx.scene.control.Label("Промокод активен");
        activeLbl.getStyleClass().add("checkbox-label");
        javafx.scene.layout.HBox activeRow = new javafx.scene.layout.HBox(8, activeBox, activeLbl);
        activeRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        activeRow.getStyleClass().add("checkbox-row");
        activeRow.setVisible(isEdit);
        activeRow.setManaged(isEdit);

        if (isEdit) {
            codeField.setText(existing.getCode() != null ? existing.getCode() : "");
            valueField.setText(existing.getDiscountValue() != null ? existing.getDiscountValue().toPlainString() : "");
            minField.setText(existing.getMinOrderAmount() != null ? existing.getMinOrderAmount().toPlainString() : "");
            maxField.setText(String.valueOf(existing.getMaxUses()));
            validField.setText(existing.getValidUntil() != null ? existing.getValidUntil().substring(0, 10) : "");
            activeBox.setSelected(existing.isActive());
        }

        codeField.textProperty().addListener((obs, o, n) -> {
            String f = n.toUpperCase().replaceAll("[^A-Z0-9\\-]", "");
            if (f.length() > 20) f = f.substring(0, 20);
            if (!f.equals(n)) { codeField.setText(f); codeField.positionCaret(f.length()); return; }
            clearFieldError(codeField, codeError);
        });
        valueField.textProperty().addListener((obs, o, n) -> {
            String f = n.replaceAll("[^0-9.]", "");
            if (f.chars().filter(c -> c == '.').count() > 1) f = o;
            if (f.length() > 10) f = f.substring(0, 10);
            if (!f.equals(n)) { valueField.setText(f); valueField.positionCaret(f.length()); return; }
            clearFieldError(valueField, valueError);
        });
        minField.textProperty().addListener((obs, o, n) -> {
            String f = n.replaceAll("[^0-9.]", "");
            if (f.chars().filter(c -> c == '.').count() > 1) f = o;
            if (f.length() > 10) f = f.substring(0, 10);
            if (!f.equals(n)) { minField.setText(f); minField.positionCaret(f.length()); }
        });
        maxField.textProperty().addListener((obs, o, n) -> {
            String f = n.replaceAll("[^0-9]", "");
            if (f.length() > 7) f = f.substring(0, 7);
            if (!f.equals(n)) { maxField.setText(f); maxField.positionCaret(f.length()); }
        });
        validField.textProperty().addListener((obs, o, n) -> {
            String digits = n.replaceAll("[^0-9]", "");
            if (digits.length() > 8) digits = digits.substring(0, 8);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 4 || i == 6) sb.append('-');
                sb.append(digits.charAt(i));
            }
            String f = sb.toString();
            if (!f.equals(n)) { validField.setText(f); validField.positionCaret(f.length()); return; }
            clearFieldError(validField, validError);
        });

        javafx.scene.layout.VBox form = new javafx.scene.layout.VBox(
            14, codeBlock, typeValueRow, minMaxRow, validBlock, activeRow);
        form.getStyleClass().add("dialog-form");

        javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Отмена");
        cancelBtn.getStyleClass().add("btn-cancel");

        javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("Сохранить");
        saveBtn.getStyleClass().add("btn-primary");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.HBox footer = new javafx.scene.layout.HBox(10, spacer, cancelBtn, saveBtn);
        footer.getStyleClass().add("dialog-footer");
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(header, form, footer);
        root.getStyleClass().add("dialog-root");
        root.setPrefWidth(480);

        Stage stage = buildStage(root, isEdit ? "Редактировать промокод" : "Новый промокод");

        cancelBtn.setOnAction(e -> stage.close());

        saveBtn.setOnAction(e -> {
            boolean ok = true;
            String code = codeField.getText().trim();
            if (code.isEmpty() || code.length() < 3) {
                showFieldError(codeField, codeError, code.isEmpty() ? "Введите код" : "Минимум 3 символа");
                ok = false;
            }
            java.math.BigDecimal value = null;
            try {
                value = new java.math.BigDecimal(valueField.getText().trim());
                if (value.compareTo(java.math.BigDecimal.ZERO) <= 0) throw new Exception();
                if ("percent".equals(typeBox.getValue()) && value.compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
                    showFieldError(valueField, valueError, "Процент не может превышать 100"); ok = false;
                }
            } catch (Exception ex2) {
                showFieldError(valueField, valueError, valueField.getText().trim().isEmpty()
                    ? "Введите размер скидки" : "Некорректное число");
                ok = false;
            }
            String validRaw = validField.getText().trim();
            if (!validRaw.isEmpty()) {
                if (!validRaw.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    showFieldError(validField, validError, "Формат: ГГГГ-ММ-ДД"); ok = false;
                } else {
                    try { java.time.LocalDate.parse(validRaw); clearFieldError(validField, validError); }
                    catch (Exception ex2) { showFieldError(validField, validError, "Неверная дата"); ok = false; }
                }
            }
            if (!ok) return;

            java.math.BigDecimal minAmount = null;
            if (!minField.getText().trim().isEmpty()) {
                try { minAmount = new java.math.BigDecimal(minField.getText().trim()); } catch (Exception ignored) {}
            }
            int maxUses = 0;
            try { maxUses = Integer.parseInt(maxField.getText().trim()); } catch (Exception ignored) {}
            String validUntil = validRaw.isEmpty() ? null : validRaw;

            saveBtn.setDisable(true);
            saveBtn.setText("Сохранение...");

            final java.math.BigDecimal fVal = value;
            final java.math.BigDecimal fMin = minAmount;
            final int fMax = maxUses;

            com.marketplace.service.PromoCodeAdminService promoService =
                new com.marketplace.service.PromoCodeAdminService();

            if (isEdit) {
                com.marketplace.util.AsyncTask.run(
                    () -> { promoService.updatePromoCode(existing.getId(), code, null,
                            typeBox.getValue(), fVal, fMin, fMax, validUntil,
                            activeBox.isSelected()); return null; },
                    v -> { com.marketplace.util.AlertUtil.showSuccess("Промокод обновлён");
                           stage.close(); if (onSaved != null) onSaved.run(); },
                    ex2 -> { saveBtn.setDisable(false); saveBtn.setText("Сохранить");
                             com.marketplace.util.AlertUtil.showError("Ошибка", ex2.getMessage()); }
                );
            } else {
                com.marketplace.util.AsyncTask.run(
                    () -> promoService.createPromoCode(code, null, typeBox.getValue(),
                            fVal, fMin, fMax, validUntil),
                    v -> { com.marketplace.util.AlertUtil.showSuccess("Промокод создан: " + v.getCode());
                           stage.close(); if (onSaved != null) onSaved.run(); },
                    ex2 -> { saveBtn.setDisable(false); saveBtn.setText("Сохранить");
                             com.marketplace.util.AlertUtil.showError("Ошибка", ex2.getMessage()); }
                );
            }
        });

        stage.showAndWait();
    }

    private static javafx.scene.control.TextField styledField(String prompt) {
        javafx.scene.control.TextField f = new javafx.scene.control.TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("styled-input");
        javafx.scene.layout.HBox.setHgrow(f, javafx.scene.layout.Priority.ALWAYS);
        return f;
    }

    private static javafx.scene.control.Label fieldLabel(String text) {
        javafx.scene.control.Label l = new javafx.scene.control.Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private static javafx.scene.control.Label errorLabel() {
        javafx.scene.control.Label l = new javafx.scene.control.Label();
        l.getStyleClass().add("field-error");
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    private static javafx.scene.layout.VBox fieldBlock(String label, String icon,
            javafx.scene.control.TextField field, javafx.scene.control.Label err) {
        javafx.scene.control.Label iconLbl = new javafx.scene.control.Label(icon);
        iconLbl.getStyleClass().add("input-icon");
        javafx.scene.layout.HBox wrapper = new javafx.scene.layout.HBox(8, iconLbl, field);
        wrapper.getStyleClass().add("input-wrapper");
        wrapper.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return new javafx.scene.layout.VBox(4, fieldLabel(label), wrapper, err);
    }

    private static void showFieldError(javafx.scene.control.TextField field,
            javafx.scene.control.Label err, String msg) {
        if (!field.getStyleClass().contains("input-error")) field.getStyleClass().add("input-error");
        err.setText(msg);
        err.setVisible(true);
        err.setManaged(true);
    }

    private static void clearFieldError(javafx.scene.control.TextField field,
            javafx.scene.control.Label err) {
        field.getStyleClass().remove("input-error");
        err.setVisible(false);
        err.setManaged(false);
    }

    private static Stage buildStage(Parent root, String title) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                DialogHelper.class.getResource(CSS_PATH).toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);
        return stage;
    }
}
