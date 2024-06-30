package com.stegano.steg0vault.models.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(0),
    USER_ALREADY_EXISTS(1);
    private final int code;
}

