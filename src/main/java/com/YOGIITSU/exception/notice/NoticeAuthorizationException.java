package com.YOGIITSU.exception.notice;

import lombok.Getter;

@Getter
public class NoticeAuthorizationException extends RuntimeException {

    private final boolean authenticated;

    public NoticeAuthorizationException(String message, boolean authenticated) {
        super(message);
        this.authenticated = authenticated;
    }
}