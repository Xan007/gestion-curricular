package org.unisoftware.gestioncurricular.frontend.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor de sesión SINGLETON para una aplicación JavaFX.
 * Seguro para uso en múltiples hilos y preparado para resets limpios.
 */
public class SessionManager {
    private static final SessionManager instance = new SessionManager();

    private volatile String token;
    private volatile String userEmail;
    private volatile String userRole;
    private volatile boolean isGuest;
    private String userId;

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

    public synchronized void setUserEmail(String email) {
        this.userEmail = email;
    }

    public synchronized String getUserEmail() {
        return userEmail;
    }

    public synchronized void setUserRole(String role) {
        this.userRole = role;
    }

    public synchronized String getUserRole() {
        return userRole;
    }

    public synchronized boolean hasRole(String role) {
        return userRole != null && userRole.equalsIgnoreCase(role);
    }

    public synchronized void setGuest(boolean guest) {
        this.isGuest = guest;
    }

    public synchronized boolean isGuest() {
        return isGuest;
    }

    public synchronized void setGuestSession() {
        this.token = null;
        this.userEmail = null;
        this.userRole = "INVITADO";
        this.isGuest = true;
    }

    public synchronized void clearSession() {
        this.token = null;
        this.userEmail = null;
        this.userRole = null;
        this.isGuest = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

