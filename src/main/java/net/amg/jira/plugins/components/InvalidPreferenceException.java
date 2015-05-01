package net.amg.jira.plugins.components;

/**
 * Thrown when user preferences are invalid during SearchService requests.
 * Created by Ivo on 01/05/15.
 */
public class InvalidPreferenceException extends Exception {
    public InvalidPreferenceException() {
    }

    public InvalidPreferenceException(String message) {
        super(message);
    }

    public InvalidPreferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPreferenceException(Throwable cause) {
        super(cause);
    }
}
