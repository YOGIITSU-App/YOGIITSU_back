package com.YOGIITSU.exception.external;

import com.YOGIITSU.exception.ErrorCode;

/**
 * Apple 토큰 교환 실패 예외
 */
public class AppleExchangeException extends ExternalServiceException {
    private final int status;
    private final String appleBody;

    public AppleExchangeException(int status, String appleBody) {
        super(ErrorCode.APPLE_AUTH_FAIL, "Apple token exchange failed: " + appleBody);
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
