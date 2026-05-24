package com.marketplace.controller.dialog;

import com.marketplace.model.PromoCode;
import com.marketplace.service.PromoCodeAdminService;
import com.marketplace.util.AlertUtil;
import com.marketplace.util.AsyncTask;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class PromoCodeDialogController {

    @FXML private Label      dialogTitleLabel;
    @FXML private TextField  codeField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField  valueField;
    @FXML private TextField  minField;
    @FXML private TextField  maxUsesField;
    @FXML private TextField  validField;
    @FXML private CheckBox   activeBox;
    @FXML private HBox       activeRow;

    @FXML private Label codeError;
    @FXML private Label valueError;
    @FXML private Label minError;
    @FXML private Label maxUsesError;
    @FXML private Label validError;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private PromoCode existing;
    private Runnable  onSaved;
    private Stage     stage;
    private final PromoCodeAdminService promoService = new PromoCodeAdminService();

    public void setExisting(PromoCode promo) { this.existing = promo; }
    public void setOnSaved(Runnable callback) { this.onSaved = callback; }
    public void setStage(Stage stage)         { this.stage = stage; }

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("percent", "fixed");
        typeBox.setValue("percent");

        setupCodeField();
        setupValueField();
        setupMinField();
        setupMaxUsesField();
        setupValidField();
    }

    public void populateIfEdit() {
        if (existing == null) {
            dialogTitleLabel.setText("Новый промокод");
            activeRow.setVisible(false);
            activeRow.setManaged(false);
            return;
        }
        dialogTitleLabel.setText("Редактировать промокод");
        codeField.setText(existing.getCode() != null ? existing.getCode() : "");
        typeBox.setValue(existing.getDiscountType() != null ? existing.getDiscountType() : "percent");
        valueField.setText(existing.getDiscountValue() != null ? existing.getDiscountValue().toPlainString() : "");
        minField.setText(existing.getMinOrderAmount() != null ? existing.getMinOrderAmount().toPlainString() : "");
        maxUsesField.setText(String.valueOf(existing.getMaxUses()));
        validField.setText(existing.getValidUntil() != null ? existing.getValidUntil().substring(0, 10) : "");
        activeBox.setSelected(existing.isActive());
        activeRow.setVisible(true);
        activeRow.setManaged(true);
    }


    private void setupCodeField() {
        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.toUpperCase().replaceAll("[^A-Z0-9\\-]", "");
            if (filtered.length() > 20) filtered = filtered.substring(0, 20);
            if (!filtered.equals(newVal)) {
                codeField.setText(filtered);
                codeField.positionCaret(filtered.length());
                return;
            }
            clearError(codeField, codeError);
        });
        codeField.focusedProperty().addListener((obs, was, now) -> {
            if (!now) validateCode(true);
        });
    }

    private void setupValueField() {
        valueField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9.]", "");
            long dots = filtered.chars().filter(c -> c == '.').count();
            if (dots > 1) filtered = oldVal;
            if (filtered.length() > 10) filtered = filtered.substring(0, 10);
            if (!filtered.equals(newVal)) {
                valueField.setText(filtered);
                valueField.positionCaret(filtered.length());
                return;
            }
            clearError(valueField, valueError);
        });
        valueField.focusedProperty().addListener((obs, was, now) -> {
            if (!now) validateValue(true);
        });
    }

    private void setupMinField() {
        minField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9.]", "");
            long dots = filtered.chars().filter(c -> c == '.').count();
            if (dots > 1) filtered = oldVal;
            if (filtered.length() > 10) filtered = filtered.substring(0, 10);
            if (!filtered.equals(newVal)) {
                minField.setText(filtered);
                minField.positionCaret(filtered.length());
                return;
            }
            clearError(minField, minError);
        });
    }

    private void setupMaxUsesField() {
        maxUsesField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("[^0-9]", "");
            if (filtered.length() > 7) filtered = filtered.substring(0, 7);
            if (!filtered.equals(newVal)) {
                maxUsesField.setText(filtered);
                maxUsesField.positionCaret(filtered.length());
                return;
            }
            clearError(maxUsesField, maxUsesError);
        });
    }

    private void setupValidField() {
        validField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 8) digits = digits.substring(0, 8);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 4 || i == 6) sb.append('-');
                sb.append(digits.charAt(i));
            }
            String formatted = sb.toString();
            if (!formatted.equals(newVal)) {
                validField.setText(formatted);
                validField.positionCaret(formatted.length());
                return;
            }
            clearError(validField, validError);
        });
        validField.focusedProperty().addListener((obs, was, now) -> {
            if (!now && !validField.getText().trim().isEmpty()) validateDate(true);
        });
    }


    private boolean validateCode(boolean showError) {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            if (showError) showError(codeField, codeError, "Введите код промокода");
            return false;
        }
        if (code.length() < 3) {
            if (showError) showError(codeField, codeError, "Минимум 3 символа");
            return false;
        }
        clearError(codeField, codeError);
        return true;
    }

    private boolean validateValue(boolean showError) {
        String raw = valueField.getText().trim();
        if (raw.isEmpty()) {
            if (showError) showError(valueField, valueError, "Введите размер скидки");
            return false;
        }
        try {
            BigDecimal val = new BigDecimal(raw);
            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                if (showError) showError(valueField, valueError, "Скидка должна быть больше 0");
                return false;
            }
            if ("percent".equals(typeBox.getValue()) && val.compareTo(BigDecimal.valueOf(100)) > 0) {
                if (showError) showError(valueField, valueError, "Процент не может превышать 100");
                return false;
            }
        } catch (Exception e) {
            if (showError) showError(valueField, valueError, "Некорректное число");
            return false;
        }
        clearError(valueField, valueError);
        return true;
    }

    private boolean validateDate(boolean showError) {
        String raw = validField.getText().trim();
        if (raw.isEmpty()) return true;
        if (!raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
            if (showError) showError(validField, validError, "Формат: ГГГГ-ММ-ДД");
            return false;
        }
        try {
            java.time.LocalDate.parse(raw);
        } catch (Exception e) {
            if (showError) showError(validField, validError, "Неверная дата");
            return false;
        }
        clearError(validField, validError);
        return true;
    }

    private boolean validateAll() {
        boolean ok = true;
        if (!validateCode(true)) ok = false;
        if (!validateValue(true)) ok = false;
        if (!validateDate(true)) ok = false;
        return ok;
    }


    @FXML
    private void onSave() {
        if (!validateAll()) return;

        String code = codeField.getText().trim();
        String discountType = typeBox.getValue();
        BigDecimal value = new BigDecimal(valueField.getText().trim());
        BigDecimal minAmount = null;
        if (!minField.getText().trim().isEmpty()) {
            try { minAmount = new BigDecimal(minField.getText().trim()); } catch (Exception ignored) {}
        }
        int maxUses = 0;
        try { maxUses = Integer.parseInt(maxUsesField.getText().trim()); } catch (Exception ignored) {}
        String validUntil = validField.getText().trim().isEmpty() ? null : validField.getText().trim();

        saveBtn.setDisable(true);
        saveBtn.setText("Сохранение...");

        final BigDecimal fVal = value;
        final BigDecimal fMin = minAmount;
        final int fMax = maxUses;

        if (existing != null) {
            AsyncTask.run(
                () -> { promoService.updatePromoCode(existing.getId(), code,
                        null, discountType, fVal, fMin,
                        fMax, validUntil, activeBox.isSelected()); return null; },
                v -> { AlertUtil.showSuccess("Промокод обновлён"); closeDialog(); if (onSaved != null) onSaved.run(); },
                ex -> { saveBtn.setDisable(false); saveBtn.setText("Сохранить");
                        AlertUtil.showError("Ошибка", ex.getMessage()); }
            );
        } else {
            AsyncTask.run(
                () -> promoService.createPromoCode(code, null, discountType, fVal, fMin, fMax, validUntil),
                v -> { AlertUtil.showSuccess("Промокод создан: " + v.getCode()); closeDialog(); if (onSaved != null) onSaved.run(); },
                ex -> { saveBtn.setDisable(false); saveBtn.setText("Сохранить");
                        AlertUtil.showError("Ошибка", ex.getMessage()); }
            );
        }
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (stage != null) {
            stage.close();
        } else {
            ((Stage) cancelBtn.getScene().getWindow()).close();
        }
    }


    private void showError(TextField field, Label errorLabel, String msg) {
        field.getStyleClass().remove("input-valid");
        if (!field.getStyleClass().contains("input-error")) field.getStyleClass().add("input-error");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError(TextField field, Label errorLabel) {
        field.getStyleClass().remove("input-error");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
