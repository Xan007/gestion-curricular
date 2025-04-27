package org.unisoftware.gestioncurricular.frontend.util;

import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestor de sesión para la aplicación frontend
 * Implementa el patrón Singleton para mantener una única instancia
 * y gestionar la información de sesión del usuario actual.
 */
public class SessionManager {

    // Instancia única (patrón Singleton)
    private static SessionManager instance;

    // Información de la sesión
    private String token;
    private String userEmail;
    private String userRole;
    private boolean isGuest;
    private UUID userId;
    private Stage primaryStage;

    // Caché para datos adicionales de la sesión
    private final Map<String, Object> sessionData = new HashMap<>();

    /**
     * Constructor privado (para el Singleton)
     */
    private SessionManager() {
        clearSession();
    }

    /**
     * Inicializar el gestor de sesión
     */
    public static void initialize() {
        if (instance == null) {
            instance = new SessionManager();
        }
    }

    /**
     * Obtener la instancia única del gestor de sesión
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    /**
     * Establecer el token de autenticación
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Obtener el token de autenticación
     */
    public String getToken() {
        return token;
    }

    /**
     * Verificar si el usuario está autenticado
     */
    public boolean isAuthenticated() {
        return token != null && !token.isEmpty() && !isGuest;
    }

    /**
     * Establecer el email del usuario
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Obtener el email del usuario
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Establecer el ID del usuario
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Obtener el ID del usuario
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Establecer el rol del usuario
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    /**
     * Obtener el rol del usuario
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Verificar si el usuario tiene un rol específico
     */
    public boolean hasRole(String role) {
        return userRole != null && userRole.equalsIgnoreCase(role);
    }

    /**
     * Establecer si el usuario es invitado
     */
    public void setGuest(boolean isGuest) {
        this.isGuest = isGuest;
    }

    /**
     * Verificar si el usuario es invitado
     */
    public boolean isGuest() {
        return isGuest;
    }

    /**
     * Configurar una sesión de invitado
     */
    public void setGuestSession() {
        clearSession();
        this.isGuest = true;
        this.userRole = "GUEST";
        this.userEmail = "guest@example.com";
    }

    /**
     * Limpiar todos los datos de la sesión
     */
    public void clearSession() {
        this.token = null;
        this.userEmail = null;
        this.userRole = null;
        this.isGuest = false;
        this.userId = null;
        this.sessionData.clear();
    }

    /**
     * Guardar la referencia a la ventana principal
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Obtener la referencia a la ventana principal
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Almacenar un dato en la sesión
     */
    public void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }

    /**
     * Recuperar un dato de la sesión
     */
    public Object getSessionData(String key) {
        return sessionData.get(key);
    }

    /**
     * Eliminar un dato de la sesión
     */
    public void removeSessionData(String key) {
        sessionData.remove(key);
    }

    /**
     * Verificar si el usuario tiene permisos para una operación específica
     * basado en su rol y estado de autenticación
     */
    public boolean hasPermission(String permission) {
        // Si es invitado, solo tiene permisos de lectura básicos
        if (isGuest) {
            return permission.startsWith("READ_") || permission.equals("VIEW_PUBLIC_CONTENT");
        }

        // Si no está autenticado, no tiene permisos
        if (!isAuthenticated()) {
            return false;
        }

        // Verificar permisos según el rol
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            // Los administradores tienen todos los permisos
            return true;
        } else if ("USER".equalsIgnoreCase(userRole)) {
            // Los usuarios normales tienen permisos específicos
            return permission.startsWith("READ_") ||
                    permission.startsWith("EDIT_OWN_") ||
                    permission.equals("CREATE_COURSE") ||
                    permission.equals("VIEW_PUBLIC_CONTENT");
        }

        // Por defecto, sin permisos específicos
        return false;
    }

    /**
     * Actualizar la información del usuario desde el backend
     * Este método se podría expandir para obtener información actualizada del usuario
     */
    public void refreshUserInfo() {
        // Aquí se implementaría la lógica para recargar información del usuario
        // como su perfil, roles, y otros datos relevantes desde el backend
        System.out.println("Actualizando información del usuario: " + userEmail);
    }
}