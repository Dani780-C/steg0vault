package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.services.UserService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "info")
    public @ResponseBody ResponseEntity<UserDTO> getInfo() {
        return new ResponseEntity<UserDTO>(userService.getUserInfo(), HttpStatus.OK);
    }

    @PostMapping(value = "change-password")
    @RolesAllowed({"USER", "ADMIN"})
    public @ResponseBody ResponseEntity<?> changePassword(@RequestBody ChangePassword changePassword) {
        userService.changePassword(changePassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "update")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<UserDTO> updateUser(@RequestBody UpdateUser updateUser) {
        return new ResponseEntity<UserDTO>(userService.updateUser(updateUser), HttpStatus.OK);
    }

    @DeleteMapping(value = "delete-account")
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<?> deleteAccount() {
        userService.deleteAccount();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
