package com.marketplace.config;

import com.marketplace.model.CartItem;
import com.marketplace.model.User;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.List;

public class AppState {
    private static AppState instance;

    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);
    private String accessToken;
    private String refreshToken;


    private boolean openRegistration = false;
    private List<CartItem> selectedCartItems;

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    public User getCurrentUser() { return currentUser.get(); }
    public void setCurrentUser(User u) { currentUser.set(u); }
    public ObjectProperty<User> currentUserProperty() { return currentUser; }

    public boolean isDarkMode() { return darkMode.get(); }
    public void setDarkMode(boolean v) { darkMode.set(v); }
    public BooleanProperty darkModeProperty() { return darkMode; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String t) { accessToken = t; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String t) { refreshToken = t; }

    public boolean isLoggedIn() { return currentUser.get() != null && accessToken != null; }

    public boolean isOpenRegistration() { return openRegistration; }
    public void setOpenRegistration(boolean openRegistration) { this.openRegistration = openRegistration; }

    public List<CartItem> getSelectedCartItems() { return selectedCartItems; }
    public void setSelectedCartItems(List<CartItem> items) { this.selectedCartItems = items; }

    public void logout() {
        currentUser.set(null);
        accessToken = null;
        refreshToken = null;
    }
}
