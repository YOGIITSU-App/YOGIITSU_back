package com.YOGIITSU.exception.apple;

public class AppleExchangeException extends RuntimeException {
    private final int status;
    private final String appleBody;

    public AppleExchangeException(int status, String appleBody) {
        super("Apple token exchange failed: " + appleBody);
        this.status = status;
        this.appleBody = appleBody;
    }

    public int getStatus() {
        return status;
    }

    public String getAppleBody() {
        return appleBody;
    }

}
