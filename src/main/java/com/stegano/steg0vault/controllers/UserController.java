package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.CollectionDTO;
import com.stegano.steg0vault.models.DTOs.CollectionResourcesDTO;
import com.stegano.steg0vault.models.DTOs.PostResourceDTO;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.entities.User;
import com.stegano.steg0vault.models.enums.RoleType;
import com.stegano.steg0vault.services.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.format.jakartajson.JsonBJsonFormatMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@RestController
@Slf4j
@RequestMapping("api/user")
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
    @GetMapping
    public ResponseEntity<User> getUser() {
        User user = userService.getUser();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

//    @PostMapping("/upload")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam("image") MultipartFile file) throws IOException {
//        String uploadImage = userService.uploadImageToFileSystem(file);
//        return ResponseEntity.status(HttpStatus.OK).body(uploadImage);
//    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/collection/{collectionName}")
    public @ResponseBody ResponseEntity<ArrayList<ResourceDTO>> getCollection(@PathVariable String collectionName) {
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
}
