package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.ExtractedResourceDTO;
import com.stegano.steg0vault.models.DTOs.PostResourceDTO;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.DTOs.UpdateResourceDTO;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.entities.User;
import com.stegano.steg0vault.services.ResourceService;
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
@CrossOrigin(
        origins = {
                "http://localhost:4200"
        },
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PATCH
        })
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
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/extract")
    public @ResponseBody ResponseEntity<ExtractedResourceDTO> getResourceAndSecret(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(resourceService.getResourceAndExtractSecret(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/info")
    public @ResponseBody ResponseEntity<Resource> getResourceInfo(@PathParam("collectionName") String collectionName, @PathParam("resourceName") String resourceName) {
        return new ResponseEntity<>(resourceService.getResourceInfo(collectionName, resourceName), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping(value = "/update")
    public @ResponseBody ResponseEntity<Boolean> updateResource(@Valid @RequestBody UpdateResourceDTO updateResourceDTO) {
        return new ResponseEntity<>(resourceService.updateResource(updateResourceDTO), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping(value = "/save")
    public @ResponseBody ResponseEntity<Boolean> saveResource() {
        return new ResponseEntity<>(resourceService.saveResource(), HttpStatus.OK);
    }

}
