package ru.turbo.goose.exceptions;

public class ImagesDoNotIntersectException extends ServiceException {

    public ImagesDoNotIntersectException() {
        super();
    }

    public ImagesDoNotIntersectException(String message) {
        super(message);
    }

    public ImagesDoNotIntersectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImagesDoNotIntersectException(Throwable cause) {
        super(cause);
    }
}
