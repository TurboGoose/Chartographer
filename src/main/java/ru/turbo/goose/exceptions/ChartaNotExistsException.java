package ru.turbo.goose.exceptions;

public class ChartaNotExistsException extends ServiceException {
    public ChartaNotExistsException() {
    }

    public ChartaNotExistsException(String message) {
        super(message);
    }

    public ChartaNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChartaNotExistsException(Throwable cause) {
        super(cause);
    }
}
