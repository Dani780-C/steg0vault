package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.entities.AuthRequest;
import com.stegano.steg0vault.models.entities.RegisterRequest;
import com.stegano.steg0vault.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
//        (
//    origins = {
//            "http://localhost:4200"
//    },
//    methods = {
//            RequestMethod.POST
//    })
public class AuthController {

    private final UserService userService;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok("{ \"token\": \"" + userService.register(request) + "\" }");
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok("{ \"token\": \"" + userService.authenticate(request) + "\"}");
    }
}
