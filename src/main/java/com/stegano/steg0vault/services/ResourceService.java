package com.stegano.steg0vault.services;

import com.stegano.steg0vault.helpers.Helper;
import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.AlgorithmEntity;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.enums.Constants;
import com.stegano.steg0vault.models.enums.ImageType;
import com.stegano.steg0vault.repositories.AlgorithmRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final SftpService sftpService;
    private final UserDetailsService userDetailsService;
    private final CollectionRepository collectionRepository;
    private final AlgorithmRepository algorithmRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            SftpService sftpService,
            UserDetailsService userDetailsService,
            CollectionRepository collectionRepository,
            AlgorithmRepository algorithmRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.sftpService = sftpService;
        this.userDetailsService = userDetailsService;
        this.collectionRepository = collectionRepository;
        this.algorithmRepository = algorithmRepository;
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

        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(collectionDTO.getName().trim(), userDetailsService.getCurrentlyLoggedUser().getId());

        AlgorithmEntity algorithm = algorithmRepository.findAlgorithmEntityByName(resourceDTO.getAlgorithm().trim());

        if(algorithm == null) {
            throw new RuntimeException();
        }

        if(collection == null) {

            Collection newCollection = Collection.builder()
                    .name(collectionDTO.getName().trim())
                    .collectionDescription(collectionDTO.getDescription().trim())
                    .user(userDetailsService.getCurrentlyLoggedUser())
                    .build();

            collectionRepository.save(newCollection);

            Resource resource = Resource.builder()
                    .name(resourceDTO.getName().trim())
                    .imageType(ImageType.convert(resourceDTO.getType()))
                    .algorithm(algorithm)
                    .description(resourceDTO.getDescription())
                    .collection(newCollection)
                    .build();

            resourceRepository.save(resource);


            Helper.saveFile(resource, resourceDTO.getImageBytes(), true);
            embedAndUploadOnSftpServer(newCollection, resource, secretToEmbed, true);
            Helper.removeFile(resource, true);

            return true;
        }

        ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
        for(Resource resource : resources) {
            if(resource.getDeletedAt() == null && resource.getName().equals(resourceDTO.getName().trim())) {
                throw new RuntimeException();
            }
        }
        Resource resource = Resource.builder()
                .name(resourceDTO.getName().trim())
                .imageType(ImageType.convert(resourceDTO.getType()))
                .algorithm(algorithm)
                .description(resourceDTO.getDescription().trim())
                .collection(collection)
                .build();

        resourceRepository.save(resource);

        Helper.saveFile(resource, resourceDTO.getImageBytes(), true);
        embedAndUploadOnSftpServer(collection, resource, secretToEmbed, false);
        Helper.removeFile(resource, true);

        return true;
    }

    public ExtractedResourceDTO tryToExtract(PostResourceDTO postResourceDTO) {
        if(postResourceDTO == null)
            throw new RuntimeException();

        ResourceDTO resourceDTO = postResourceDTO.getResourceDTO();

        if(resourceDTO == null)
            throw new RuntimeException();


        Resource resource = Resource.builder()
                .name("TryToExtract")
                .imageType(ImageType.convert(resourceDTO.getType()))
                .build();

        Helper.saveFile(resource, resourceDTO.getImageBytes(), true);

        CoverImage coverImage = new CoverImage();
        coverImage.readImage(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());

        List<AlgorithmEntity> algorithmEntities = algorithmRepository.findAll();

        for (AlgorithmEntity algorithmEntity : algorithmEntities) {
            Algorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmEntity.getName());
            try {
                Secret secret = algorithm.extract(coverImage);
                if (!secret.getDecryptedMessage().equals("There is no embedded message!")
                   && !secret.getDecryptedMessage().equals("Cannot decrypt")) {
                    Helper.removeFile(resource, true);
                    return ExtractedResourceDTO.builder()
                            .algorithm(algorithmEntity.getName())
                            .message(secret.getDecryptedMessage())
                            .imageBytes("")
                            .build();
                }
            } catch (Exception ignored) {
            }
        }

        Helper.removeFile(resource, true);
        return ExtractedResourceDTO.builder()
                .algorithm("NONE")
                .message("Sorry! Cannot find a secret in this image!")
                .imageBytes("")
                .build();
    }

    void embedAndUploadOnSftpServer(Collection collection, Resource resource, String secretToEmbed, boolean newColl) {
        Secret secret = new Secret(secretToEmbed);
        CoverImage coverImage = new CoverImage();
        coverImage.readImage(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());
        Algorithm algorithm = AlgorithmFactory.createAlgorithm(resource.getAlgorithm().getName());
        algorithm.embed(coverImage, secret);
        coverImage.save(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
        if(newColl) sftpService.createCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection.getName());
        sftpService.uploadFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource);
    }

    public ExtractedResourceDTO getResourceAndExtractSecret(String collectionName, String resourceName) {

        if(collectionName == null || resourceName == null)
            throw new RuntimeException();

        Collection collection = this.collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(
                collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null) {
            throw new RuntimeException();
        }

        Resource resource = this.resourceRepository.getResourceByNameAndCollectionIdAndDeletedAtIsNull(resourceName, collection.getId());

        if(resource == null) {
            throw new RuntimeException();
        }

        ResourceDTO resourceDTO = sftpService.getResource(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection.getName(), resource);

        Helper.saveFile(resource, resourceDTO.getImageBytes(), true);

        CoverImage coverImage = new CoverImage();
        coverImage.readImage(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());

        Algorithm algorithm = AlgorithmFactory.createAlgorithm(resource.getAlgorithm().getName());
        Secret secret = algorithm.extract(coverImage);

        Helper.removeFile(resource, true);

        return ExtractedResourceDTO.builder()
                .imageBytes(resourceDTO.getImageBytes())
                .algorithm(resourceDTO.getAlgorithm())
                .name(resourceDTO.getName())
                .message(secret.getDecryptedMessage())
                .build();
    }

    @Transactional
    public boolean updateResource(String collectionName, String resourceName, UpdateResourceDTO updateResourceDTO) {

        if(collectionName == null || resourceName == null || updateResourceDTO == null)
            throw new RuntimeException();

        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(
                collectionName,
                userDetailsService.getCurrentlyLoggedUser().getId()
        );

        if(collection == null)
            throw new RuntimeException();

        Resource resource = resourceRepository.getResourceByNameAndCollectionIdAndDeletedAtIsNull(
                resourceName.trim(),
                collection.getId()
        );

        if(resource == null)
            throw new RuntimeException();

        if(updateResourceDTO.getNewCollection() == null)
            throw new RuntimeException();

        if(!updateResourceDTO.getNewCollection().equals(collection.getName())) {
            Collection collectionToMove = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(
                    updateResourceDTO.getNewCollection(),
                    userDetailsService.getCurrentlyLoggedUser().getId()
            );

            if (collectionToMove == null)
                throw new RuntimeException();

            if(updateResourceDTO.getName() == null)
                throw new RuntimeException();

            for (Resource rsc : collectionToMove.getResources()) {
                if (rsc.getDeletedAt() == null && rsc.getName().equals(updateResourceDTO.getName().trim()))
                    throw new RuntimeException();
            }

            ResourceDTO resourceDTO = sftpService.getResource(
                    userDetailsService.getCurrentlyLoggedUser().getEmail(),
                    collection.getName(),
                    resource
            );

            String oldName = resource.getImageName();
            resource.setName(updateResourceDTO.getName().trim());
            Helper.saveFile(resource, resourceDTO.getImageBytes(), false);
            sftpService.deleteFile(
                    userDetailsService.getCurrentlyLoggedUser().getEmail(),
                    collection,
                    oldName
            );
            resource.setCollection(collectionToMove);
            sftpService.uploadFile(
                    userDetailsService.getCurrentlyLoggedUser().getEmail(),
                    collectionToMove,
                    resource
            );
            Helper.removeFile(resource, false);
            List<Resource> resourceList = collection.getResources().stream().filter(rsc -> rsc.getDeletedAt() == null).toList();
            if(resourceList.size() == 1) {
                collection.setDeletedAt(LocalDateTime.now());
                sftpService.deleteCollection(
                        userDetailsService.getCurrentlyLoggedUser().getEmail(),
                        collection
                );
                collectionRepository.save(collection);
            }
        }
        else {
            if(updateResourceDTO.getName() == null)
                throw new RuntimeException();

            if(!resource.getName().equals(updateResourceDTO.getName())) {
                for (Resource rsc : collection.getResources()) {
                    if (rsc.getDeletedAt() == null && rsc.getName().equals(updateResourceDTO.getName()))
                        throw new RuntimeException();
                }
            }

            ResourceDTO resourceDTO = sftpService.getResource(
                    userDetailsService.getCurrentlyLoggedUser().getEmail(),
                    collection.getName(),
                    resource
            );

            String oldName = resource.getImageName();
            resource.setName(updateResourceDTO.getName());
            Helper.saveFile(resource, resourceDTO.getImageBytes(), false);
            sftpService.deleteFile(
                    userDetailsService.getCurrentlyLoggedUser().getEmail(),
                    collection,
                    oldName
            );
            sftpService.uploadFile(
                    userDetailsService.getCurrentlyLoggedUser().getEmail(),
                    collection,
                    resource
            );
            Helper.removeFile(resource, false);
        }

        if(updateResourceDTO.getDescription() == null)
            throw new RuntimeException();

        resource.setDescription(updateResourceDTO.getDescription());
        resourceRepository.save(resource);

        if(updateResourceDTO.getNewSecret() != null && !updateResourceDTO.getNewSecret().isEmpty()) {
            embedAndUploadOnSftpServerOnUpdate(resource.getCollection(), resource, updateResourceDTO.getNewSecret());
        }

        return true;
    }

    private void embedAndUploadOnSftpServerOnUpdate(Collection collection, Resource resource, String newSecret) {
        ResourceDTO resourceDTO = sftpService.getResource(
                userDetailsService.getCurrentlyLoggedUser().getEmail(),
                resource.getCollection().getName(),
                resource
        );
        Helper.saveFile(resource, resourceDTO.getImageBytes(), true);

        Secret secret = new Secret(newSecret);
        CoverImage coverImage = new CoverImage();
        coverImage.readImage(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());
        Algorithm algorithm = AlgorithmFactory.createAlgorithm(resource.getAlgorithm().getName());
        algorithm.embed(coverImage, secret);
        sftpService.deleteFile(
                userDetailsService.getCurrentlyLoggedUser().getEmail(),
                resource.getCollection(),
                resource.getImageName()
        );
        coverImage.save(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
        sftpService.uploadFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource);
        Helper.removeFile(resource, true);
    }


    @Transactional
    public boolean updateCollection(String collectionName, UpdateCollectionDTO updateCollectionDTO) {

        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null)
            throw new RuntimeException();

        if(updateCollectionDTO == null) return true;
        if(updateCollectionDTO.getName() == null) return true;

        Collection collectionExist = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(updateCollectionDTO.getName(), userDetailsService.getCurrentlyLoggedUser().getId());

        if(collectionExist != null && collection.getName().equals(collectionExist.getName())) {
            if(updateCollectionDTO.getDescription() != null) {
                collection.setCollectionDescription(updateCollectionDTO.getDescription());
                collectionRepository.save(collection);
            }
            return true;
        }

        if(collectionExist != null)
            throw new RuntimeException();

        String oldName = collection.getName();
        collection.setName(updateCollectionDTO.getName());
        sftpService.updateCollectionName(userDetailsService.getCurrentlyLoggedUser().getEmail(), oldName, updateCollectionDTO.getName());

        if(updateCollectionDTO.getDescription() != null)
            collection.setCollectionDescription(updateCollectionDTO.getDescription());

        collectionRepository.save(collection);
        return true;
    }

    public ResourceDTO getResourceInfo(String collectionName, String resourceName) {

        Collection collection = this.collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(
                collectionName, userDetailsService.getCurrentlyLoggedUser().getId()
        );

        if(collection == null) {
            throw new RuntimeException();
        }

        Resource resource = this.resourceRepository.getResourceByNameAndCollectionIdAndDeletedAtIsNull(resourceName, collection.getId());

        if(resource == null) {
            throw new RuntimeException();
        }

        return ResourceDTO.builder()
                .type(resource.getImageType().toString())
                .name(resource.getName())
                .description(resource.getDescription())
                .algorithm(resource.getAlgorithm().getName())
                .createdAt(resource.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                .modifiedAt(resource.getModifiedAt() != null? resource.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                .imageBytes("")
                .build();
    }

    public boolean deleteResource(String collectionName, String resourceName) {
        if(collectionName == null || resourceName == null)
            throw new RuntimeException();

        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null)
            throw new RuntimeException();

        Resource resource = resourceRepository.getResourceByNameAndCollectionIdAndDeletedAtIsNull(resourceName, collection.getId());

        if(resource == null)
            throw new RuntimeException();

        resource.setDeletedAt(LocalDateTime.now());
        resourceRepository.save(resource);
        sftpService.deleteFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource.getImageName());


        List<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId()).stream().filter(rsc -> rsc.getDeletedAt() == null).toList();
        if(resources.isEmpty()) {
            collection.setDeletedAt(LocalDateTime.now());
            collectionRepository.save(collection);
            sftpService.deleteCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection);
        }

        return true;
    }

    public boolean deleteCollection(String collectionName) {
        if(collectionName == null)
            throw new RuntimeException();

        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null)
            throw new RuntimeException();

        List<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId()).stream().filter(rsc -> rsc.getDeletedAt() == null).toList();

        for(Resource resource : resources) {
            sftpService.deleteFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource.getImageName());
            resource.setDeletedAt(LocalDateTime.now());
            resourceRepository.save(resource);
        }

        collection.setDeletedAt(LocalDateTime.now());
        collectionRepository.save(collection);
        sftpService.deleteCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection);

        return true;
    }

    public ImageBytes getImage(String collectionName, String resourceName) {
        if(collectionName == null || resourceName == null)
            throw new RuntimeException();
        Collection collection = collectionRepository.getCollectionByNameAndUserIdAndDeletedAtIsNull(
                collectionName,
                userDetailsService.getCurrentlyLoggedUser().getId()
        );
        if(collection == null)
            throw new RuntimeException();

        Resource resource = resourceRepository.getResourceByNameAndCollectionIdAndDeletedAtIsNull(
                resourceName,
                collection.getId()
        );

        if(resource == null)
            throw new RuntimeException();

        ResourceDTO resourceDTO = sftpService.getResource(
                userDetailsService.getCurrentlyLoggedUser().getEmail(),
                collection.getName(),
                resource
        );

        return ImageBytes.builder()
                .imageBytes(resourceDTO.getImageBytes())
                .build();
    }

    public List<String> getAllAlgs() {
        List<String> algorithms = new ArrayList<>();
        List<AlgorithmEntity> algorithmEntities = algorithmRepository.findAll();

        for(AlgorithmEntity algorithm : algorithmEntities)
            if(algorithm.getDeletedAt() == null)
                algorithms.add(algorithm.getName());

        return algorithms;
    }
}
