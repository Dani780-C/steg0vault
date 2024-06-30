package com.stegano.steg0vault.services;

import com.stegano.steg0vault.exceptions.CollectionNotFoundException;
import com.stegano.steg0vault.models.DTOs.CollectionDTO;
import com.stegano.steg0vault.models.DTOs.CollectionResourcesDTO;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.DTOs.ResourceNameAndDescriptionDTO;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.repositories.CollectionRepository;
import com.stegano.steg0vault.repositories.ResourceRepository;
import com.stegano.steg0vault.sftp.SftpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
@Slf4j
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final ResourceRepository resourceRepository;
    private final UserDetailsService userDetailsService;
    private final SftpService sftpService;

    public CollectionService(CollectionRepository collectionRepository, ResourceRepository resourceRepository, UserDetailsService userDetailsService, SftpService sftpService) {
        this.collectionRepository = collectionRepository;
        this.resourceRepository = resourceRepository;
        this.userDetailsService = userDetailsService;
        this.sftpService = sftpService;
    }

    public ArrayList<ResourceDTO> getCollection(String collectionName) {
        if(collectionName == null || collectionName.isEmpty())
            throw new CollectionNotFoundException();
        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(
                collectionName,
                userDetailsService.getCurrentlyLoggedUser().getId()
        );

        if(collection == null || collection.getDeletedAt() != null)
            return new ArrayList<>();

        ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
        return sftpService.getResources(userDetailsService.getCurrentlyLoggedUser().getEmail(), collectionName, resources);
    }

    public ArrayList<CollectionResourcesDTO> getCollections() {
        ArrayList<Collection> collections = collectionRepository.getCollectionsByUserId(userDetailsService.getCurrentlyLoggedUser().getId());
        ArrayList<CollectionResourcesDTO> collectionResourcesDTOS = new ArrayList<>();

        for (Collection collection : collections) {
            if(collection.getDeletedAt() == null) {
                ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
                ArrayList<ResourceNameAndDescriptionDTO> resourceNameAndDescriptionDTOS = new ArrayList<>();
                for (Resource resource : resources) {
                    if(resource.getDeletedAt() == null) {
                        resourceNameAndDescriptionDTOS.add(
                                ResourceNameAndDescriptionDTO.builder()
                                        .name(resource.getName())
                                        .description(resource.getDescription())
                                        .createdAt(resource.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                                        .modifiedAt(resource.getModifiedAt() != null? resource.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                                        .build()
                        );
                    }
                }
                CollectionDTO collectionDTO = CollectionDTO.builder()
                        .name(collection.getName())
                        .createdAt(collection.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                        .modifiedAt(collection.getModifiedAt() != null? collection.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                        .description(collection.getCollectionDescription())
                        .build();
                collectionResourcesDTOS.add(
                        CollectionResourcesDTO.builder()
                                .collectionDTO(collectionDTO)
                                .resourceNameAndDescriptionDTO(resourceNameAndDescriptionDTOS)
                                .build()
                );
            }
        }
        return collectionResourcesDTOS;
    }
}
