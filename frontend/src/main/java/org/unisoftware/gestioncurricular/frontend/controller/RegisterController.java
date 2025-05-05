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
import org.unisoftware.gestioncurricular.security.auth.SupabaseAuthService;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

import java.io.IOException;
import java.util.Collections;

@Component
public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @Autowired
    private SupabaseAuthService authService;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Maneja el evento de registro de usuario
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validaciones básicas
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Por favor, complete todos los campos");
            messageLabel.getStyleClass().add("error-message");
            return;
        }
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Las contraseñas no coinciden");
            messageLabel.getStyleClass().add("error-message");
            return;
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            messageLabel.setText("Formato de correo electrónico incorrecto");
            messageLabel.getStyleClass().add("error-message");
            return;
        }
        if (password.length() < 6) {
            messageLabel.setText("La contraseña debe tener al menos 6 caracteres");
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

            // Intento de registro con el servicio de autenticación
            String token = authService.signUp(email, password);

            // Guardar el token en el gestor de sesión
            SessionManager.getInstance().setToken(token);
            SessionManager.getInstance().setUserEmail(email);
            SessionManager.getInstance().setUserRole("USER");
            SessionManager.getInstance().setGuest(false);

            // Navegar a la pantalla principal
            navigateToMainScreen(event);

        } catch (Exception e) {
            messageLabel.setText("Error al registrar usuario: " + e.getMessage());
            messageLabel.getStyleClass().add("error-message");
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            stage.setScene(loginScene);
            stage.setWidth(ancho);
            stage.setHeight(alto);
            stage.setTitle("Iniciar Sesión");
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error al cargar la pantalla de inicio de sesión");
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
            stage.setScene(mainScene);
            stage.setTitle("Gestión Curricular");
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error al cargar la pantalla principal");
            messageLabel.getStyleClass().add("error-message");
            e.printStackTrace();
        }
    }
}