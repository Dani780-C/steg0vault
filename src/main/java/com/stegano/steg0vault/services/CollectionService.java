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
        Collection collection = collectionRepository.getCollectionByNameAndUserId(
                collectionName,
                userDetailsService.getCurrentlyLoggedUser().getId()
        );

        ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
        return sftpService.getResources(userDetailsService.getCurrentlyLoggedUser().getEmail(), collectionName, resources);
    }

    public ArrayList<CollectionResourcesDTO> getCollections() {
        ArrayList<Collection> collections = collectionRepository.getCollectionsByUserId(userDetailsService.getCurrentlyLoggedUser().getId());
        ArrayList<CollectionResourcesDTO> collectionResourcesDTOS = new ArrayList<>();

        for (Collection collection : collections) {
            ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
            ArrayList<ResourceNameAndDescriptionDTO> resourceNameAndDescriptionDTOS = new ArrayList<>();
            for (Resource resource : resources) {
                resourceNameAndDescriptionDTOS.add(
                        ResourceNameAndDescriptionDTO.builder()
                                .name(resource.getName())
                                .description(resource.getDescription())
                                .build()
                );
            }
            CollectionDTO collectionDTO = CollectionDTO.builder()
                    .name(collection.getName())
                    .description(collection.getCollectionDescription())
                    .build();
            collectionResourcesDTOS.add(
                    CollectionResourcesDTO.builder()
                            .collectionDTO(collectionDTO)
                            .resourceNameAndDescriptionDTO(resourceNameAndDescriptionDTOS)
                            .build()
            );
        }
        return collectionResourcesDTOS;
    }
}
