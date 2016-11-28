package io.card.cordova.sdk;


public class TelpoException extends Exception {
    private static final long serialVersionUID = 1136193940236894072L;

    public TelpoException() {
    }

    public TelpoException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TelpoException(String detailMessage) {
        super(detailMessage);
    }

    public TelpoException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Exception occur during telpo device operation!";
    }
}
