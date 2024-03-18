package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.entities.User;
import com.stegano.steg0vault.services.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@Slf4j
@RequestMapping("/api/user")
@CrossOrigin(
    origins = {
            "http://localhost:4200"
    },
    methods = {
            RequestMethod.POST,
            RequestMethod.GET
    })
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/collection/")
    public @ResponseBody ResponseEntity<ArrayList<ResourceDTO>> getCollection(@PathParam("collectionName") String collectionName) {
        log.info("controller");
        return new ResponseEntity<>(userService.getCollection(collectionName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/all/collection")
    public @ResponseBody ResponseEntity<ArrayList<CollectionResourcesDTO>> getCollections() {
        log.info("controller in get all collections");
        return new ResponseEntity<>(userService.getCollections(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/upload-resource")
    public @ResponseBody ResponseEntity<Boolean> postResource(@Valid @RequestBody PostResourceDTO postResourceDTO) {
        log.info("controller in post resource");
        return new ResponseEntity<>(userService.postResource(postResourceDTO), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/get-resource/")
    public @ResponseBody ResponseEntity<ExtractedResourceDTO> getExtractedResource(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(userService.getExtractedResource(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/get-resource-info/")
    public @ResponseBody ResponseEntity<Resource> getResourceInfo(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(userService.getResourceInfo(collectionName, resourceName), HttpStatus.OK);
    }
}
