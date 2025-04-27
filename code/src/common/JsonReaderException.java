package src.common;

public class JsonReaderException extends RuntimeException {
    public JsonReaderException() {
        super();
    }

    public JsonReaderException(String message) {
        super(message);
    }

    public JsonReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonReaderException(Throwable cause) {
        super(cause);
    }
}
