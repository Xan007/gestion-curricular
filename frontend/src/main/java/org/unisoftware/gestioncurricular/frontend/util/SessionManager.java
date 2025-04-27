package org.unisoftware.gestioncurricular.frontend.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor de sesión SINGLETON para una aplicación JavaFX.
 * Seguro para uso en múltiples hilos y preparado para resets limpios.
 */
public class SessionManager {
    // Instancia singleton, segura para JavaFX y Spring contextos
    private static final SessionManager instance = new SessionManager();

    private volatile String token;
    private volatile String userEmail;
    private volatile List<String> userRoles;
    private volatile boolean isGuest;

    private SessionManager() {
        clearSession();
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public synchronized void setToken(String token) {
        this.token = token;
    }

    public synchronized String getToken() {
        return token;
    }

    public synchronized void setUserEmail(String username) {
        this.userEmail = username;
    }

    public synchronized String getUserEmail() {
        return userEmail;
    }

    public synchronized void setUserRoles(List<String> roles) {
        this.userRoles = (roles == null) ? Collections.emptyList() : new ArrayList<>(roles);
    }

    public synchronized List<String> getUserRoles() {
        return userRoles == null ? Collections.emptyList() : new ArrayList<>(userRoles);
    }

    public synchronized boolean hasRole(String role) {
        List<String> rolesSnapshot = userRoles;
        if (rolesSnapshot == null) return false;
        return rolesSnapshot.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    public synchronized void setGuest(boolean guest) {
        isGuest = guest;
    }

    public synchronized boolean isGuest() {
        return isGuest;
    }

    public synchronized void setGuestSession() {
        this.token = null;
        this.userEmail = null;
        this.userRoles = Collections.singletonList("INVITADO");
        this.isGuest = true;
    }

    /**
     * Limpia todos los datos de sesión
     */
    public synchronized void clearSession() {
        this.token = null;
        this.userEmail = null;
        this.userRoles = Collections.emptyList();
        this.isGuest = false;
    }
}