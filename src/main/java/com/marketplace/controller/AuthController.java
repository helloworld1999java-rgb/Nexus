package com.marketplace.controller;

import com.marketplace.config.AppState;
import com.marketplace.service.AuthService;
import com.marketplace.util.AsyncTask;
import com.marketplace.util.ErrorUtil;
import com.marketplace.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AuthController {

    @FXML private VBox loginPane;
    @FXML private VBox registerPane;

    @FXML private TextField     loginEmailField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Button        loginButton;
    @FXML private Label         loginErrorLabel;

    @FXML private TextField     registerNameField;
    @FXML private TextField     registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmField;
    @FXML private Button        registerButton;
    @FXML private Label         registerErrorLabel;

    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
        if (loadingIndicator != null) loadingIndicator.setVisible(false);

        if (AppState.getInstance().isOpenRegistration()) {
            AppState.getInstance().setOpenRegistration(false);
            showRegister();
        }
    }

    @FXML
    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String pass  = loginPasswordField.getText();
        if (email.isEmpty() || pass.isEmpty()) { showLoginError("Введите email и пароль"); return; }
        loginButton.setDisable(true);
        loginErrorLabel.setVisible(false);
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        AsyncTask.run(
                () -> authService.signIn(email, pass),
                user -> {
                    AppState.getInstance().setCurrentUser(user);
                    SceneManager.getInstance().switchTo("main");
                    if (MainController.getInstance() != null) {
                        MainController.getInstance().updateNavVisibility();
                    }
                },
                ex -> {
                    loginButton.setDisable(false);
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    showLoginError(ErrorUtil.friendlyMessage(ex));
                }
        );
    }

    @FXML
    private void handleRegister() {
        String name    = registerNameField.getText().trim();
        String email   = registerEmailField.getText().trim();
        String pass    = registerPasswordField.getText();
        String confirm = registerConfirmField.getText();
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) { showRegError("Заполните все поля"); return; }
        if (!pass.equals(confirm)) { showRegError("Пароли не совпадают"); return; }
        if (pass.length() < 6)    { showRegError("Пароль минимум 6 символов"); return; }
        registerButton.setDisable(true);
        registerErrorLabel.setVisible(false);
        AsyncTask.run(
                () -> authService.signUp(email, pass, name),
                user -> {
                    AppState.getInstance().setCurrentUser(user);
                    SceneManager.getInstance().switchTo("main");
                    if (MainController.getInstance() != null) {
                        MainController.getInstance().updateNavVisibility();
                    }
                },
                ex -> { registerButton.setDisable(false); showRegError(ErrorUtil.friendlyMessage(ex)); }
        );
    }

    @FXML
    private void showRegister() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }

    @FXML
    private void showLogin() {
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
    }

    private void showLoginError(String msg) { loginErrorLabel.setText(msg); loginErrorLabel.setVisible(true); }
    private void showRegError(String msg)   { registerErrorLabel.setText(msg); registerErrorLabel.setVisible(true); }
}
