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
import java.util.List;

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

    @Transactional
    public boolean updateResource(String collectionName, String resourceName, UpdateResourceDTO updateResourceDTO) {
        System.out.println(updateResourceDTO.getName());
        System.out.println(updateResourceDTO.getDescription());
        System.out.println(updateResourceDTO.getNewSecret());
        System.out.println(updateResourceDTO.getAlgorithm());

        // TODO: update resource
        if(collectionName == null || resourceName == null)
            throw new RuntimeException();

        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null)
            throw new RuntimeException();

        Resource resource = resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null)
            throw new RuntimeException();
        String oldFilename = resource.getImageName();

        ResourceDTO resourceDTO = sftpService.getResource(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection.getName(), resource);

//        if(updateResourceDTO.getDescription() != null)
//            resource.setDescription(updateResourceDTO.getDescription());
//
//        if(updateResourceDTO.getName() != null && !updateResourceDTO.getName().isEmpty()) {
//            List<Resource> resourceList = resourceRepository.getResourcesByCollectionId(collection.getId());
//            int i = 0;
//            while(i < resourceList.size() && !(resourceList.get(i).getName().trim().equals(updateResourceDTO.getName().trim()))) i++;
//            if(i == resourceList.size())
//                resource.setName(updateResourceDTO.getName().trim());
//        }

        Secret secret = new Secret();
        if(updateResourceDTO.getAlgorithm() != null) {
            if(updateResourceDTO.getAlgorithm().equals(resource.getAlgorithmType().toString())) {
                if(updateResourceDTO.getNewSecret() != null && !updateResourceDTO.getNewSecret().isEmpty()) {
                    // TODO: same alg but different secret
                    secret = new Secret(updateResourceDTO.getNewSecret());
                }
                else {
                    ExtractedResourceDTO extractedResourceDTO = getResourceAndExtractSecret(collectionName, resourceName);
                    secret = new Secret(extractedResourceDTO.getMessage());
                }
            }
            else {
                if(updateResourceDTO.getNewSecret() != null) {
                    if(updateResourceDTO.getNewSecret().isEmpty()) {
                        // TODO: extract message and embed with new alg
//                        Helper.saveFile(resource, resourceDTO.getImageBytes());
//                        CoverImage coverImage = new CoverImage();
//                        coverImage.readImage("./" + Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
//                        Algorithm algorithm = AlgorithmFactory.createAlgorithm(resource.getAlgorithmType());
                        ExtractedResourceDTO extractedResourceDTO = getResourceAndExtractSecret(collectionName, resourceName);
                        secret = new Secret(extractedResourceDTO.getMessage());
//                        Helper.removeFile(resource);
                    }
                    else {
                        //TODO: both alg and secret are different
                        secret = new Secret(updateResourceDTO.getNewSecret());
                    }
                    resource.setAlgorithmType(AlgorithmType.valueOf(updateResourceDTO.getAlgorithm()));
                }
            }

            if(updateResourceDTO.getDescription() != null)
                resource.setDescription(updateResourceDTO.getDescription());

            if(updateResourceDTO.getName() != null && !updateResourceDTO.getName().isEmpty()) {
                List<Resource> resourceList = resourceRepository.getResourcesByCollectionId(collection.getId());
                int i = 0;
                while(i < resourceList.size() && !(resourceList.get(i).getName().trim().equals(updateResourceDTO.getName().trim()))) i++;
                if(i == resourceList.size())
                    resource.setName(updateResourceDTO.getName().trim());
            }

            Helper.saveFile(resource, resourceDTO.getImageBytes());

            sftpService.deleteFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, oldFilename);

            embedAndUploadOnSftpServer(collection, resource, secret.getRealSecret());

            sftpService.uploadFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource);

            System.out.println(" HHHHHHEEEEEEEEEEEEEEERRRRRRRRRRRRRREEEEEEEEEEEEEEEEE ");
            Helper.removeFile(resource);
            resourceRepository.save(resource);
        }

        return true;
    }

    public boolean saveResource(String collectionName, String resourceName) {

        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());
        if(collection == null)
            throw new RuntimeException();
        Resource resource = resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null)
            throw new RuntimeException();



        resource.setSaved(!resource.isSaved());
        resourceRepository.save(resource);
        return resource.isSaved();
    }

    public ResourceDTO getResourceInfo(String collectionName, String resourceName) {

        Collection collection = this.collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null) {
            throw new RuntimeException();
        }

        Resource resource = this.resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null) {
            throw new RuntimeException();
        }

        return ResourceDTO.builder()
                .type(resource.getImageType().toString())
                .name(resource.getName())
                .description(resource.getDescription())
                .algorithm(resource.getAlgorithmType().toString())
                .imageBytes("")
                .isSaved(resource.isSaved())
                .build();
    }

    public boolean deleteResource(String collectionName, String resourceName) {
        if(collectionName == null || resourceName == null)
            throw new RuntimeException();

        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null)
            throw new RuntimeException();

        Resource resource = resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null)
            throw new RuntimeException();

        List<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());

        resourceRepository.delete(resource);
        sftpService.deleteFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource.getImageName());

        if(resources.size() == 1) {
            collectionRepository.delete(collection);
            sftpService.deleteCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection);
        }

        return true;
    }

    public boolean deleteCollection(String collectionName) {
        if(collectionName == null)
            throw new RuntimeException();

        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null)
            throw new RuntimeException();

        List<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());

        for(Resource resource : resources) {
            sftpService.deleteFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource.getImageName());
            resourceRepository.delete(resource);
        }

        collectionRepository.delete(collection);
        sftpService.deleteCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection);

        return true;
    }

}
