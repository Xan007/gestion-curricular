package org.unisoftware.gestioncurricular.exception;

public class SupabaseException extends RuntimeException {

    private final int statusCode;
    private final String errorMessage;

    public SupabaseException(int statusCode, String errorMessage) {
        super("Supabase Error: " + errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
