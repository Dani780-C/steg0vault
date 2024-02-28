package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.entities.User;
import com.stegano.steg0vault.services.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/resource")
@CrossOrigin(
        origins = {
                "http://localhost:4200"
        },
        methods = {
                RequestMethod.GET
        })
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value="/get/{collectionName}/")
    public ResponseEntity<List<ResourceDTO>> getResources(@PathVariable String collectionName, @RequestParam(value="resources") List<String> resources) {
        return new ResponseEntity<>(resourceService.getResources(collectionName, resources), HttpStatus.OK);
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping(value="/get-bytes/{collectionName}/{resourceName}")
    public ResponseEntity<String> getResourceBytes(@PathVariable String collectionName, @PathVariable String resourceName) {
        return new ResponseEntity<>(resourceService.getResourceBytesByResourceName(collectionName, resourceName), HttpStatus.OK);
    }

}
