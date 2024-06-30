package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.RegisterRequest;
import com.stegano.steg0vault.services.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/admin")
@CrossOrigin
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping(value = "/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<List<UserInfoDTO>> getAllUsers() {
        return new ResponseEntity<List<UserInfoDTO>>(adminService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping(value = "/algs/all")
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody ResponseEntity<List<AlgDTO>> getAllAlgs() {
        return new ResponseEntity<List<AlgDTO>>(adminService.getAllAlgs(), HttpStatus.OK);
    }

    @PostMapping(value = "/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody RegisterRequest registerRequest) {
        adminService.createAdmin(registerRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/alg/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAlgorithm(@RequestBody String createAlgorithm) {
        adminService.createAlgorithm(createAlgorithm);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/get-details/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDetails(@PathVariable Long id) {
        return new ResponseEntity<>(adminService.getDetails(id), HttpStatus.OK);
    }

    @PostMapping(value = "/delete/{id}/{banned}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> markAsInactive(@PathVariable Long id, @PathVariable String banned) {
        adminService.markAsInactive(id, banned);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/alg/disable/{algorithmName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableAlg(@PathVariable String algorithmName) {
        adminService.disableAlg(algorithmName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/alg/enable/{algorithmName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableAlg(@PathVariable String algorithmName) {
        adminService.enableAlg(algorithmName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/all/images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllImages(@PathVariable Long id) {
        return new ResponseEntity<List<Long>>(adminService.getAllImages(id), HttpStatus.OK);
    }

    @GetMapping(value = "/image/{id}/{resourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageBytes> getImage(@PathVariable Long id, @PathVariable Long resourceId) {
        return new ResponseEntity<>(adminService.getImage(id, resourceId), HttpStatus.OK);
    }

    @GetMapping(value = "/alg/info/{algName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAlgInfo(@PathVariable String algName) {
        return new ResponseEntity<>(adminService.getAlgInfo(algName), HttpStatus.OK);
    }
}
