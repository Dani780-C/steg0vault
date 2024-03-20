package com.stegano.steg0vault.services;

import com.stegano.steg0vault.helpers.Helper;
import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.enums.AlgorithmType;
import com.stegano.steg0vault.models.enums.Constants;
import com.stegano.steg0vault.models.enums.ImageType;
import com.stegano.steg0vault.repositories.CollectionRepository;
import com.stegano.steg0vault.repositories.ResourceRepository;
import com.stegano.steg0vault.sftp.SftpService;
import com.stegano.steg0vault.stego.algorithms.Algorithm;
import com.stegano.steg0vault.stego.algorithms.AlgorithmFactory;
import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final SftpService sftpService;
    private final UserDetailsService userDetailsService;
    private final CollectionRepository collectionRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            SftpService sftpService,
            UserDetailsService userDetailsService,
            CollectionRepository collectionRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.sftpService = sftpService;
        this.userDetailsService = userDetailsService;
        this.collectionRepository = collectionRepository;
    }

    @Transactional
    public Boolean postResource(PostResourceDTO postResourceDTO) {

        if(postResourceDTO == null)
            throw new RuntimeException();

        CollectionDTO collectionDTO = postResourceDTO.getCollectionDTO();
        ResourceDTO resourceDTO = postResourceDTO.getResourceDTO();
        String secretToEmbed = postResourceDTO.getSecretToEmbed();

        if(collectionDTO == null) {
            throw new RuntimeException();
        } else if (!collectionDTO.valid()) {
            throw new RuntimeException();
        }
        if(resourceDTO == null) {
            throw new RuntimeException();
        } else if (!resourceDTO.valid()) {
            throw new RuntimeException();
        }
        if(secretToEmbed == null || secretToEmbed.isEmpty()) {
            throw new RuntimeException();
        }

        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionDTO.getName(), userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null) {

            Collection newCollection = Collection.builder()
                    .name(collectionDTO.getName().trim())
                    .collectionDescription(collectionDTO.getDescription())
                    .user(userDetailsService.getCurrentlyLoggedUser())
                    .build();

            collectionRepository.save(newCollection);

            sftpService.createCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), newCollection.getName());

            Resource resource = Resource.builder()
                    .name(resourceDTO.getName().trim())
                    .isSaved(false)
                    .imageType(ImageType.convert(resourceDTO.getType()))
                    .algorithmType(AlgorithmType.convert((resourceDTO.getAlgorithm())))
                    .description(resourceDTO.getDescription())
                    .collection(newCollection)
                    .build();

            resourceRepository.save(resource);


            Helper.saveFile(resource, resourceDTO.getImageBytes());
            embedAndUploadOnSftpServer(newCollection, resource, secretToEmbed);
            Helper.removeFile(resource);

            return true;
        }

        ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
        for(Resource resource : resources) {
            if(resource.getName().equals(resourceDTO.getName())) {
                throw new RuntimeException();
            }
        }
        Resource resource = Resource.builder()
                .name(resourceDTO.getName())
                .isSaved(false)
                .imageType(ImageType.convert(resourceDTO.getType()))
                .algorithmType(AlgorithmType.convert((resourceDTO.getAlgorithm())))
                .description(resourceDTO.getDescription())
                .collection(collection)
                .build();

        resourceRepository.save(resource);

        Helper.saveFile(resource, resourceDTO.getImageBytes());
        embedAndUploadOnSftpServer(collection, resource, secretToEmbed);
        Helper.removeFile(resource);

        return true;
    }

    void embedAndUploadOnSftpServer(Collection collection, Resource resource, String secretToEmbed) {
        Secret secret = new Secret(secretToEmbed);
        CoverImage coverImage = new CoverImage();
        coverImage.readImage(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());
        Algorithm algorithm = AlgorithmFactory.createAlgorithm(resource.getAlgorithmType());
        algorithm.embed(coverImage, secret);
        coverImage.save(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
        sftpService.uploadFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource);
    }

    public ExtractedResourceDTO getResourceAndExtractSecret(String collectionName, String resourceName) {

        if(collectionName == null || resourceName == null)
            throw new RuntimeException();

        Collection collection = this.collectionRepository.getCollectionByNameAndUserId(
                collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null) {
            throw new RuntimeException();
        }

        Resource resource = this.resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null) {
            throw new RuntimeException();
        }

        ResourceDTO resourceDTO = sftpService.getResource(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection.getName(), resource);

        Helper.saveFile(resource, resourceDTO.getImageBytes());

        CoverImage coverImage = new CoverImage();
        coverImage.readImage(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());
        Algorithm algorithm = AlgorithmFactory.createAlgorithm(resource.getAlgorithmType());
        Secret secret = algorithm.extract(coverImage);

        Helper.removeFile(resource);

        return ExtractedResourceDTO.builder()
                .imageBytes(resourceDTO.getImageBytes())
                .algorithm(resourceDTO.getAlgorithm())
                .name(resourceDTO.getName())
                .message(secret.getRealSecret())
                .build();
    }

    public boolean updateResource(UpdateResourceDTO updateResourceDTO) {
        return false;
    }

    public boolean saveResource() {
        return true;
    }

    public Resource getResourceInfo(String collectionName, String resourceName) {

        Collection collection = this.collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null) {
            throw new RuntimeException();
        }

        Resource resource = this.resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null) {
            throw new RuntimeException();
        }

        return resource;
    }

}
