package org.unisoftware.gestioncurricular.frontend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainScreenController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userTypeLabel;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Button logoutButton;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager sessionManager = SessionManager.getInstance();

        // Establecer el mensaje de bienvenida según el tipo de usuario
        String userEmail = sessionManager.getUserEmail();

        if (sessionManager.isGuest()) {
            welcomeLabel.setText("Bienvenido, Invitado");

            // Configurar interfaz para usuario invitado
            setupGuestUI();

            if (userTypeLabel != null) {
                userTypeLabel.setText("Modo: Invitado (Visualización limitada)");
            }
        } else {
            welcomeLabel.setText("Bienvenido, " + userEmail);

            if (userTypeLabel != null) {
                String role = sessionManager.getUserRole();
                userTypeLabel.setText("Modo: " + (role != null ? role : "Usuario registrado"));
            }
        }
    }

    /**
     * Configura la interfaz para usuarios invitados con permisos limitados
     */
    private void setupGuestUI() {
        // Deshabilitar pestañas restringidas para invitados
        if (mainTabPane != null) {
            for (Tab tab : mainTabPane.getTabs()) {
                if ("Administración".equals(tab.getText())) {
                    tab.setDisable(true);
                }
            }
        }

        // Deshabilitar elementos restringidos recursivamente
        findAndDisableButtons(mainTabPane);
    }

    /**
     * Método recursivo para encontrar y deshabilitar botones restringidos en la interfaz
     */
    private void findAndDisableButtons(Node node) {
        if (node == null) return;

        // Deshabilitar botones específicos basados en su ID o texto
        if (node instanceof Button) {
            Button button = (Button) node;
            if (button.getText() != null &&
                    (button.getText().contains("Editar") ||
                            button.getText().contains("Eliminar") ||
                            button.getText().contains("Agregar") ||
                            button.getText().contains("Guardar") ||
                            button.getText().contains("Nuevo"))) {
                button.setDisable(true);
                button.setTooltip(new Tooltip("Disponible solo para usuarios registrados"));
            }
        }

        // Verificar nodos hijos recursivamente
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                findAndDisableButtons(child);
            }
        }
    }

    /**
     * Maneja el cierre de sesión
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        // Limpiar la sesión del usuario
        SessionManager.getInstance().clearSession();

        try {
            // Volver a la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Iniciar Sesión - Gestión Curricular");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al navegar a la pantalla de login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navega a la pestaña de cursos
     */
    @FXML
    public void handleViewCourses(ActionEvent event) {
        // Navegar a la pestaña de cursos
        for (Tab tab : mainTabPane.getTabs()) {
            if ("Cursos".equals(tab.getText())) {
                mainTabPane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    /**
     * Maneja la edición del perfil de usuario
     */
    @FXML
    public void handleEditProfile(ActionEvent event) {
        // Si es un usuario invitado, mostrar un mensaje de restricción
        if (SessionManager.getInstance().isGuest()) {
            showRestrictedAccessAlert();
            return;
        }

        // Aquí iría la lógica para editar el perfil
        showNotImplementedAlert("Edición de Perfil");
    }

    /**
     * Maneja la adición de un nuevo curso
     */
    @FXML
    public void handleAddCourse(ActionEvent event) {
        // Si es un usuario invitado, mostrar un mensaje de restricción
        if (SessionManager.getInstance().isGuest()) {
            showRestrictedAccessAlert();
            return;
        }

        // Aquí iría la lógica para agregar un curso
        showNotImplementedAlert("Agregar Curso");
    }

    /**
     * Maneja la actualización de la lista de cursos
     */
    @FXML
    public void handleRefreshCourses(ActionEvent event) {
        // Aquí iría la lógica para recargar los cursos
        showNotImplementedAlert("Actualizar lista de cursos");
    }

    /**
     * Maneja la gestión de usuarios (solo admin)
     */
    @FXML
    public void handleUsersManagement(ActionEvent event) {
        // Verificar si el usuario tiene permisos de administrador
        if (!SessionManager.getInstance().hasRole("ADMIN")) {
            showRestrictedAccessAlert();
            return;
        }

        // Aquí iría la lógica para la gestión de usuarios
        showNotImplementedAlert("Gestión de Usuarios");
    }

    /**
     * Maneja la configuración del sistema (solo admin)
     */
    @FXML
    public void handleSystemConfig(ActionEvent event) {
        // Verificar si el usuario tiene permisos de administrador
        if (!SessionManager.getInstance().hasRole("ADMIN")) {
            showRestrictedAccessAlert();
            return;
        }

        // Aquí iría la lógica para la configuración del sistema
        showNotImplementedAlert("Configuración del Sistema");
    }

    /**
     * Muestra un mensaje de acceso restringido
     */
    private void showRestrictedAccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acceso Restringido");
        alert.setHeaderText("Funcionalidad Limitada");
        alert.setContentText("Esta funcionalidad solo está disponible para usuarios registrados.");
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de funcionalidad no implementada
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText("Funcionalidad en Desarrollo");
        alert.setContentText("La funcionalidad '" + feature + "' se implementará próximamente.");
        alert.showAndWait();
    }
}