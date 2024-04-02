package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.CollectionResourcesDTO;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.services.CollectionService;
import com.stegano.steg0vault.services.ResourceService;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@Slf4j
@RequestMapping("/api/v1/collection")
@CrossOrigin
//        (
//        origins = {
//                "http://localhost:4200"
//        },
//        methods = {
//                RequestMethod.GET
//        })
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "")
    public @ResponseBody ResponseEntity<ArrayList<ResourceDTO>> getCollection(@PathParam("collectionName") String collectionName) {
        return new ResponseEntity<>(collectionService.getCollection(collectionName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/all")
    public @ResponseBody ResponseEntity<ArrayList<CollectionResourcesDTO>> getCollections() {
        return new ResponseEntity<>(collectionService.getCollections(), HttpStatus.OK);
    }
}
