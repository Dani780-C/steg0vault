package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.ResetPassword;
import com.stegano.steg0vault.services.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/forgot-pass")
@CrossOrigin
public class ForgotPasswordController {
    private final UserService userService;

    public ForgotPasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/request")
    public ResponseEntity<?> sendMailForForgotPassword(@PathParam("mail") String mail) {
        userService.forgotPassword(mail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPassword resetPassword) {
        userService.resetPassword(resetPassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
