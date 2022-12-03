package app.exceptions;

public class TDERuntimeException extends RuntimeException {
    public TDERuntimeException() {
    }

    public TDERuntimeException(String message) {
        super(message);
    }

    public TDERuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TDERuntimeException(Throwable cause) {
        super(cause);
    }

    public TDERuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
