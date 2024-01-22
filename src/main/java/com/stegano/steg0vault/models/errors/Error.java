package com.stegano.steg0vault.models.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Error {

    private int errorCode;

    private String errorMessage;
}
