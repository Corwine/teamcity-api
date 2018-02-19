package com.clocktower.teamcity.api.exceptions;

public class FailedAuthorizationException extends TeamCityException {

    public FailedAuthorizationException(Throwable cause) {
        super(cause);
    }

    public FailedAuthorizationException(String message) {
        super(message);
    }

    public FailedAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
