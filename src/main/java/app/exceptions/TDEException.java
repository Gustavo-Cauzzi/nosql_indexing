package app.exceptions;

public class TDEException extends Exception {
    public TDEException() {
    }

    public TDEException(String message) {
        super(message);
    }

    public TDEException(String message, Throwable cause) {
        super(message, cause);
    }

    public TDEException(Throwable cause) {
        super(cause);
    }

    public TDEException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
