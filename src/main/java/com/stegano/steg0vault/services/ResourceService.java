package com.stegano.steg0vault.services;

import com.stegano.steg0vault.exceptions.CollectionNotFoundException;
import com.stegano.steg0vault.exceptions.ResourceNotFoundException;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.entities.User;
import com.stegano.steg0vault.repositories.CollectionRepository;
import com.stegano.steg0vault.repositories.ResourceRepository;
import com.stegano.steg0vault.sftp.SftpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final SftpService sftpService;
    private final UserService userService;
    private final CollectionRepository collectionRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            SftpService sftpService,
            UserService userService,
            CollectionRepository collectionRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.sftpService = sftpService;
        this.userService = userService;
        this.collectionRepository = collectionRepository;
    }

    public String getResourceBytesByResourceName(String collectionName, String resourceName) {
        User user = userService.getCurrentlyLoggedUser();
        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, user.getId());
        if(collection == null) {
            throw new CollectionNotFoundException();
        }
        Resource resource = resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());
        if(resource == null) {
            throw new ResourceNotFoundException();
        }
        return sftpService.getResource(user.getEmail(), collectionName, resource).getImageBytes();
    }

    public List<ResourceDTO> getResources(String collectionName, List<String> resources) {
        System.out.println("here");
        User user = userService.getCurrentlyLoggedUser();
        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, user.getId());
        if(collection == null) {
            throw new CollectionNotFoundException();
        }
        List<ResourceDTO> resourceDTOS = new ArrayList<>();
        for (String resourceName : resources) {
            Resource resource = resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());
            if(resource == null) {
                throw new ResourceNotFoundException();
            }
            ResourceDTO resourceDTO = sftpService.getResource(user.getEmail(), collectionName, resource);
            if(resourceDTO == null) {
                throw new ResourceNotFoundException();
            }
            resourceDTOS.add(resourceDTO);
        }
        return resourceDTOS;
    }
}
