package com.stegano.steg0vault;

import com.stegano.steg0vault.exceptions.UserAlreadyExistsException;
import com.stegano.steg0vault.exceptions.UserNotFoundException;
import com.stegano.steg0vault.models.errors.Error;
import com.stegano.steg0vault.models.errors.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists() {
        return new ResponseEntity<>(
                new Error(
                        ErrorCode.USER_ALREADY_EXISTS.getCode(),
                        "User already exists!"
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound() {
        return new ResponseEntity<>(
                new Error(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        "User not found!"
                ),
                HttpStatus.NOT_FOUND
        );
    }
}
