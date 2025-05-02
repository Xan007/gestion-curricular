package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.security.auth.AuthTokens;
import org.unisoftware.gestioncurricular.security.auth.SupabaseAuthService;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.service.UserServiceFront;
import org.unisoftware.gestioncurricular.frontend.dto.UserInfoDTO;

import java.io.IOException;
import java.util.Collections;

@Component
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @Autowired
    private SupabaseAuthService authService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserServiceFront userServiceFront;

    /**
     * Maneja el evento de inicio de sesión
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validaciones básicas
        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Por favor, complete todos los campos");
            messageLabel.getStyleClass().add("error-message");
            return;
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            messageLabel.setText("Formato de correo electrónico incorrecto");
            messageLabel.getStyleClass().add("error-message");
            return;
        }

        try {
            // Verificar si el servicio está correctamente inyectado
            if (authService == null) {
                messageLabel.setText("Error: Servicio de autenticación no disponible");
                messageLabel.getStyleClass().add("error-message");
                return;
            }

            // Intento de inicio de sesión con el servicio de autenticación
            AuthTokens tokens = authService.signIn(email, password);

            String token = tokens.getAccessToken();

            // Guardar token
            SessionManager.getInstance().setToken(token);
            SessionManager.getInstance().setUserEmail(email);

            // Obtener detalles reales del usuario desde el backend y guardarlos en la sesión
            UserInfoDTO userInfo = userServiceFront.getCurrentUserInfo();
            if (userInfo != null) {
                SessionManager.getInstance().setUserRoles(userInfo.getRoles());
                SessionManager.getInstance().setUserEmail(userInfo.getEmail());
            } else {
                SessionManager.getInstance().setUserRoles(Collections.emptyList());
            }
            SessionManager.getInstance().setGuest(false);

            // Navegar a la pantalla principal
            navigateToMainScreen(event);

        } catch (Exception e) {
            messageLabel.setText("Error al iniciar sesión: " + e.getMessage());
            messageLabel.getStyleClass().add("error-message");
            e.printStackTrace();
        }
    }

    /**
     * Maneja el evento de acceso como invitado
     */
    @FXML
    public void loginAsGuest(ActionEvent event) {
        try {
            SessionManager.getInstance().setGuestSession();
            navigateToMainScreen(event);
        } catch (Exception e) {
            messageLabel.setText("Error al iniciar sesión como invitado: " + e.getMessage());
            messageLabel.getStyleClass().add("error-message");
            e.printStackTrace();
        }
    }

    /**
     * Navega a la pantalla de registro
     */
    @FXML
    public void navigateToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Parent registerView = loader.load();
            Scene registerScene = new Scene(registerView);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            stage.setScene(registerScene);
            stage.setWidth(ancho);
            stage.setHeight(alto);
            stage.setTitle("Registro de Usuario");
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error al cargar la pantalla de registro");
            messageLabel.getStyleClass().add("error-message");
            e.printStackTrace();
        }
    }

    private void navigateToMainScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainScreen.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Parent mainView = loader.load();
            Scene mainScene = new Scene(mainView);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            stage.setScene(mainScene);
            stage.setWidth(ancho);
            stage.setHeight(alto);
            stage.setTitle("Gestión Curricular");
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error al cargar la pantalla principal");
            messageLabel.getStyleClass().add("error-message");
            e.printStackTrace();
        }
    }
}
