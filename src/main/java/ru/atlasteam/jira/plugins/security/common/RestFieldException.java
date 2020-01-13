package ru.atlasteam.jira.plugins.security.common;

public class RestFieldException extends IllegalArgumentException {
    private final String field;

    public RestFieldException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
