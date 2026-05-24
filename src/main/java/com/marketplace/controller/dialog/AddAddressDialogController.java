package com.marketplace.controller.dialog;

import com.marketplace.model.Address;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class AddAddressDialogController {

    @FXML private TextField labelField;
    @FXML private TextField cityField;
    @FXML private TextField streetField;
    @FXML private TextField houseField;
    @FXML private TextField entranceField;
    @FXML private TextField apartmentField;
    @FXML private CheckBox  defaultCheckbox;

    @FXML private Label labelError;
    @FXML private Label cityError;
    @FXML private Label streetError;
    @FXML private Label houseError;
    @FXML private Label previewCity;
    @FXML private Label previewStreet;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Consumer<Address> onSaveCallback;
    private Runnable onCancelCallback;
    private Address existingAddress;

    @FXML
    public void initialize() {
        setupValidation(labelField,  labelError,  2, 50,  "Название");
        setupValidation(cityField,   cityError,   2, 80,  "Город");
        setupValidation(streetField, streetError, 2, 120, "Улица");
        setupValidation(houseField,  houseError,  1, 20,  "Дом");
        cityField.textProperty().addListener((o,a,b)   -> updatePreview());
        streetField.textProperty().addListener((o,a,b) -> updatePreview());
        houseField.textProperty().addListener((o,a,b)  -> updatePreview());
    }

    private void updatePreview() {
        String city = cityField.getText().trim();
        String st   = streetField.getText().trim();
        String h    = houseField.getText().trim();
        if (previewCity != null) previewCity.setText(city.isEmpty() ? "Город..." : city);
        StringBuilder sb = new StringBuilder();
        if (!st.isEmpty()) sb.append("ул. ").append(st);
        if (!h.isEmpty())  { if (sb.length()>0) sb.append(", "); sb.append("д. ").append(h); }
        if (previewStreet != null)
            previewStreet.setText(sb.length()==0 ? "Улица и дом будут показаны здесь" : sb.toString());
    }

    public void prefill(Address addr) {
        this.existingAddress = addr;
        if (addr == null) return;
        setOrEmpty(labelField,     addr.getLabel());
        setOrEmpty(cityField,      addr.getCity());
        setOrEmpty(streetField,    addr.getStreet());
        setOrEmpty(houseField,     addr.getBuilding());
        setOrEmpty(apartmentField, addr.getApartment());
        defaultCheckbox.setSelected(addr.isDefault());
        if (saveBtn != null) saveBtn.setText("Сохранить");
        updatePreview();
    }

    public void setOnSave(Consumer<Address> cb) { this.onSaveCallback = cb; }
    public void setOnCancel(Runnable cb)         { this.onCancelCallback = cb; }

    private void setupValidation(TextField f, Label err, int min, int max, String name) {
        f.textProperty().addListener((o,a,n) -> {
            if (n.length() > max) { f.setText(n.substring(0, max)); return; }
            hideError(err);
        });
        f.focusedProperty().addListener((o,a,now) -> { if (!now) validate(f,err,min,name,true); });
    }

    private boolean validate(TextField f, Label err, int min, String name, boolean show) {
        String v = f.getText().trim();
        if (v.isEmpty()) { if (show) showError(err,"⚠ Поле «"+name+"» обязательно"); return false; }
        if (v.length()<min) { if (show) showError(err,"⚠ "+name+": минимум "+min+" символа"); return false; }
        hideError(err); return true;
    }

    @FXML
    private void onSave() {
        boolean ok = validate(labelField,labelError,2,"Название",true)
                   & validate(cityField,cityError,2,"Город",true)
                   & validate(streetField,streetError,2,"Улица",true)
                   & validate(houseField,houseError,1,"Дом",true);
        if (!ok) return;
        saveBtn.setDisable(true); saveBtn.setText("Сохранение...");
        Address addr = existingAddress != null ? existingAddress : new Address();
        addr.setLabel(labelField.getText().trim());
        addr.setCity(cityField.getText().trim());
        addr.setStreet(streetField.getText().trim());
        addr.setBuilding(houseField.getText().trim());
        String apt = apartmentField.getText().trim();
        String ent = entranceField != null ? entranceField.getText().trim() : "";
        if (!ent.isEmpty() && !apt.isEmpty()) addr.setApartment("подъезд "+ent+", кв. "+apt);
        else if (!ent.isEmpty()) addr.setApartment("подъезд "+ent);
        else addr.setApartment(apt);
        addr.setDefault(defaultCheckbox.isSelected());
        if (onSaveCallback != null) onSaveCallback.accept(addr);
        close();
    }

    @FXML
    private void onCancel() { if (onCancelCallback!=null) onCancelCallback.run(); close(); }

    private void close() { ((Stage) saveBtn.getScene().getWindow()).close(); }

    private void showError(Label l, String msg) { if(l!=null){l.setText(msg);l.setVisible(true);l.setManaged(true);} }
    private void hideError(Label l)              { if(l!=null){l.setVisible(false);l.setManaged(false);} }
    private void setOrEmpty(TextField f, String v) { if(f!=null) f.setText(v!=null?v:""); }
}
