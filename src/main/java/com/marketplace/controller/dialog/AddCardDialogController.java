package com.marketplace.controller.dialog;

import com.marketplace.model.PaymentCard;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.YearMonth;
import java.util.function.BiConsumer;

public class AddCardDialogController {

    @FXML private TextField     cardNumberField;
    @FXML private TextField     cardHolderField;
    @FXML private TextField     monthField;
    @FXML private TextField     yearField;
    @FXML private PasswordField cvvField;
    @FXML private CheckBox      defaultCheckbox;

    @FXML private Label cardNumberPreview;
    @FXML private Label cardHolderPreview;
    @FXML private Label cardExpiryPreview;
    @FXML private Label cardTypeLabel;
    @FXML private Label cardTypeIcon;

    @FXML private Label cardNumberError;
    @FXML private Label cardHolderError;
    @FXML private Label expiryError;
    @FXML private Label cvvError;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private BiConsumer<PaymentCard, String> onSaveCallback;
    private Runnable onCancelCallback;

    @FXML
    public void initialize() {
        setupCardNumberField();
        setupCardHolderField();
        setupExpiryFields();
        setupCvvField();
    }

    public void setOnSave(BiConsumer<PaymentCard, String> callback) { this.onSaveCallback = callback; }
    public void setOnCancel(Runnable callback)                       { this.onCancelCallback = callback; }


    private void setupCardNumberField() {
        cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("\\D", "");
            if (digits.length() > 16) digits = digits.substring(0, 16);
            String formatted = formatCardNumber(digits);
            if (!formatted.equals(newVal)) {
                cardNumberField.setText(formatted);
                cardNumberField.positionCaret(formatted.length());
                return;
            }
            updateCardNumberPreview(digits);
            updateCardType(digits);
            clearError(cardNumberField, cardNumberError);
        });

        cardNumberField.focusedProperty().addListener((obs, was, now) -> {
            if (!now) validateCardNumber(cardNumberField.getText().replaceAll("\\D", ""), true);
        });
    }

    private void updateCardNumberPreview(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(i < digits.length() ? digits.charAt(i) : '•');
        }
        cardNumberPreview.setText(sb.toString());
    }

    private void updateCardType(String digits) {
        String type = detectCardType(digits);
        switch (type) {
            case "visa"       -> { cardTypeLabel.setText("VISA");       cardTypeIcon.setText("💳"); }
            case "mastercard" -> { cardTypeLabel.setText("MASTERCARD"); cardTypeIcon.setText("💳"); }
            case "mir"        -> { cardTypeLabel.setText("МИР");        cardTypeIcon.setText("💳"); }
            case "amex"       -> { cardTypeLabel.setText("AMEX");       cardTypeIcon.setText("💳"); }
            default           -> { cardTypeLabel.setText("КАРТА");      cardTypeIcon.setText("💳"); }
        }
    }

    private String detectCardType(String digits) {
        if (digits.startsWith("4"))                                         return "visa";
        if (digits.startsWith("34") || digits.startsWith("37"))            return "amex";
        if (digits.startsWith("2200") || digits.startsWith("2201") ||
                digits.startsWith("2202") || digits.startsWith("2203") ||
                digits.startsWith("2204"))                                  return "mir";
        if (digits.startsWith("5") || digits.startsWith("2"))              return "mastercard";
        return "unknown";
    }

    private String formatCardNumber(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    private boolean validateCardNumber(String digits, boolean showError) {
        if (digits.length() != 16) {
            if (showError)
                setError(cardNumberField, cardNumberError,
                        "⚠ Номер карты должен содержать 16 цифр (введено: " + digits.length() + ")");
            return false;
        }
        setValid(cardNumberField, cardNumberError);
        return true;
    }


    private void setupCardHolderField() {
        cardHolderField.textProperty().addListener((obs, oldVal, newVal) -> {
            String upper = newVal.toUpperCase()
                    .replaceAll("[^A-Z ]", "")
                    .replaceAll(" {2,}", " ");
            if (!upper.equals(newVal)) {
                cardHolderField.setText(upper);
                cardHolderField.positionCaret(upper.length());
                return;
            }
            cardHolderPreview.setText(upper.isEmpty() ? "ИМЯ ДЕРЖАТЕЛЯ" : upper);
            clearError(cardHolderField, cardHolderError);
        });
        cardHolderField.focusedProperty().addListener((obs, was, now) -> {
            if (!now) validateCardHolder(true);
        });
    }

    private boolean validateCardHolder(boolean showError) {
        String val = cardHolderField.getText().trim();
        if (val.isEmpty()) {
            if (showError) setError(cardHolderField, cardHolderError, "⚠ Введите имя держателя карты");
            return false;
        }
        if (val.length() < 3) {
            if (showError) setError(cardHolderField, cardHolderError, "⚠ Слишком короткое имя");
            return false;
        }
        if (!val.contains(" ")) {
            if (showError) setError(cardHolderField, cardHolderError, "⚠ Введите имя и фамилию");
            return false;
        }
        setValid(cardHolderField, cardHolderError);
        return true;
    }


    private void setupExpiryFields() {
        monthField.textProperty().addListener((obs, old, newVal) -> {
            String digits = newVal.replaceAll("\\D", "");
            if (digits.length() > 2) digits = digits.substring(0, 2);
            if (!digits.equals(newVal)) { monthField.setText(digits); return; }
            updateExpiryPreview();
            if (digits.length() == 2) yearField.requestFocus();
            clearError(monthField, expiryError);
        });
        yearField.textProperty().addListener((obs, old, newVal) -> {
            String digits = newVal.replaceAll("\\D", "");
            if (digits.length() > 4) digits = digits.substring(0, 4);
            if (!digits.equals(newVal)) { yearField.setText(digits); return; }
            updateExpiryPreview();
            clearError(yearField, expiryError);
        });
        yearField.focusedProperty().addListener((obs, was, now)  -> { if (!now) validateExpiry(true); });
        monthField.focusedProperty().addListener((obs, was, now) -> { if (!now && !yearField.isFocused()) validateExpiry(true); });
    }

    private void updateExpiryPreview() {
        String m = monthField.getText(), y = yearField.getText();
        cardExpiryPreview.setText((m.isEmpty() ? "ММ" : m) + "/" + (y.isEmpty() ? "ГГГГ" : y));
    }

    private boolean validateExpiry(boolean showError) {
        try {
            int month = Integer.parseInt(monthField.getText().trim());
            int year  = Integer.parseInt(yearField.getText().trim());
            if (month < 1 || month > 12) {
                if (showError) setError(monthField, expiryError, "⚠ Месяц: от 01 до 12");
                return false;
            }
            if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
                if (showError) setError(monthField, expiryError, "✗ Срок действия карты истёк");
                return false;
            }
        } catch (NumberFormatException e) {
            if (showError) setError(monthField, expiryError, "⚠ Введите корректную дату (ММ / ГГГГ)");
            return false;
        }
        setValid(monthField, expiryError);
        setValid(yearField, null);
        return true;
    }


    private void setupCvvField() {
        cvvField.textProperty().addListener((obs, old, newVal) -> {
            String digits = newVal.replaceAll("\\D", "");
            if (digits.length() > 4) digits = digits.substring(0, 4);
            if (!digits.equals(newVal)) cvvField.setText(digits);
            clearError(cvvField, cvvError);
        });
        cvvField.focusedProperty().addListener((obs, was, now) -> { if (!now) validateCvv(true); });
    }

    private boolean validateCvv(boolean showError) {
        if (cvvField.getText().trim().length() < 3) {
            if (showError) setError(cvvField, cvvError, "⚠ CVV: 3–4 цифры");
            return false;
        }
        setValid(cvvField, cvvError);
        return true;
    }


    @FXML
    private void onSave() {
        String digits = cardNumberField.getText().replaceAll("\\D", "");
        boolean ok = validateCardNumber(digits, true)
                & validateCardHolder(true)
                & validateExpiry(true)
                & validateCvv(true);
        if (!ok) return;

        saveBtn.setDisable(true);
        saveBtn.setText("Сохранение...");

        PaymentCard card = new PaymentCard();
        card.setCardNumberMasked(digits.substring(digits.length() - 4));
        card.setCardHolder(cardHolderField.getText().trim().toUpperCase());
        card.setExpiryMonth(Integer.parseInt(monthField.getText().trim()));
        card.setExpiryYear(Integer.parseInt(yearField.getText().trim()));
        card.setDefault(defaultCheckbox.isSelected());
        card.setCardType(detectCardType(digits));

        try {
            if (onSaveCallback != null) onSaveCallback.accept(card, digits);
            close();
        } catch (Exception e) {
            saveBtn.setDisable(false);
            saveBtn.setText("Сохранить");
            setError(saveBtn, null, "⚠ Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        if (onCancelCallback != null) onCancelCallback.run();
        close();
    }

    private void close() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }


    private void setError(Control field, Label errorLabel, String message) {
        if (field != null) {
            field.getParent().getStyleClass().remove("input-valid");
            if (!field.getParent().getStyleClass().contains("input-error"))
                field.getParent().getStyleClass().add("input-error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void setValid(Control field, Label errorLabel) {
        if (field != null) {
            field.getParent().getStyleClass().remove("input-error");
            if (!field.getParent().getStyleClass().contains("input-valid"))
                field.getParent().getStyleClass().add("input-valid");
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearError(Control field, Label errorLabel) {
        if (field != null) {
            field.getParent().getStyleClass().remove("input-error");
            field.getParent().getStyleClass().remove("input-valid");
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}