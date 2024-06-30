package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.services.ResourceService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/resource")
@CrossOrigin
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/upload")
    public @ResponseBody ResponseEntity<Boolean> postResource(@Valid @RequestBody PostResourceDTO postResourceDTO) {
        return new ResponseEntity<>(resourceService.postResource(postResourceDTO), HttpStatus.OK);
    }

    @PostMapping(value = "/try-to-extract")
    public @ResponseBody ResponseEntity<ExtractedResourceDTO> tryToExtractResource(@RequestBody PostResourceDTO postResourceDTO) {
        return new ResponseEntity<>(resourceService.tryToExtract(postResourceDTO), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/extract")
    public @ResponseBody ResponseEntity<ExtractedResourceDTO> getResourceAndSecret(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(resourceService.getResourceAndExtractSecret(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/info")
    public @ResponseBody ResponseEntity<ResourceDTO> getResourceInfo(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(resourceService.getResourceInfo(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/algs/all")
    public @ResponseBody ResponseEntity<List<String>> getResourceInfo() {
        return new ResponseEntity<>(resourceService.getAllAlgs(), HttpStatus.OK);
    }

    @RolesAllowed({"USER", "ADMIN"})
    @GetMapping(value = "/get-image")
    public @ResponseBody ResponseEntity<ImageBytes> getImage(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<ImageBytes>(resourceService.getImage(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping(value = "/update")
    public @ResponseBody ResponseEntity<Boolean> updateResource(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName, @Valid @RequestBody UpdateResourceDTO updateResourceDTO) {
        return new ResponseEntity<>(resourceService.updateResource(collectionName, resourceName, updateResourceDTO), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping(value = "/update/coll")
    public @ResponseBody ResponseEntity<Boolean> updateCollection(@PathParam("collectionName") String collectionName, @Valid @RequestBody UpdateCollectionDTO updateCollectionDTO) {
        return new ResponseEntity<>(resourceService.updateCollection(collectionName, updateCollectionDTO), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value = "/delete")
    public @ResponseBody ResponseEntity<Boolean> deleteResource(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(resourceService.deleteResource(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(value = "/delete-coll")
    public @ResponseBody ResponseEntity<Boolean> deleteCollection(@PathParam("collectionName") String collectionName) {
        return new ResponseEntity<>(resourceService.deleteCollection(collectionName), HttpStatus.OK);
    }
}
