package com.stegano.steg0vault.services;

import com.stegano.steg0vault.exceptions.UserAlreadyExistsException;
import com.stegano.steg0vault.exceptions.UserNotFoundException;
import com.stegano.steg0vault.helpers.SaveImageLocallyHelper;
import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.*;
import com.stegano.steg0vault.models.enums.AlgorithmType;
import com.stegano.steg0vault.models.enums.Constants;
import com.stegano.steg0vault.models.enums.ImageType;
import com.stegano.steg0vault.models.enums.RoleType;
import com.stegano.steg0vault.repositories.CollectionRepository;
import com.stegano.steg0vault.repositories.ResourceRepository;
import com.stegano.steg0vault.repositories.UserRepository;
import com.stegano.steg0vault.security.JwtService;
import com.stegano.steg0vault.sftp.sftpService;
import com.stegano.steg0vault.stego.algorithms.Algorithm;
import com.stegano.steg0vault.stego.algorithms.LsbReplacementAlgorithm;
import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ResourceRepository resourceRepository;
    private final CollectionRepository collectionRepository;
    private final sftpService sftpService;

    public UserService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            ResourceRepository resourceRepository,
            CollectionRepository collectionRepository,
            sftpService sftpService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.resourceRepository = resourceRepository;
        this.collectionRepository = collectionRepository;
        this.sftpService = sftpService;
    }

    public String register(RegisterRequest request) {
        User usr = getUserByEmail(request.getEmail());
        if (usr != null) {
            throw new UserAlreadyExistsException();
        }
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleType(RoleType.USER)
                .build();
        this.createUserSpace(user);
        Collection collection = this.createDefaultCollection(user);
        this.createDefaultResource(user, collection);
        return jwtService.generateToken(user);
    }

    private void createUserSpace(User user) {
        sftpService.createUserSpace(user.getEmail());
        userRepository.save(user);
    }

    private Collection createDefaultCollection(User user) {
        Collection collection = Collection.builder()
                .name(Constants.DEFAULT_COLLECTION_NAME.getValue())
                .collectionDescription(Constants.DEFAULT_COLLECTION_DESCRIPTION.getValue())
                .user(user)
                .build();
        sftpService.createCollection(user.getEmail(), Constants.DEFAULT_COLLECTION_NAME.getValue());
        collectionRepository.save(collection);
        return collection;
    }

    private void createDefaultResource(User user, Collection collection) {
        Resource resource = Resource.builder()
                .name(Constants.DEFAULT_RESOURCE_NAME.getValue())
                .description(Constants.DEFAULT_COLLECTION_DESCRIPTION.getValue())
                .imageType(ImageType.PNG)
                .algorithmType(AlgorithmType.A_TYPE1)
                .collection(collection)
                .build();
        // TODO: HERE I AM
    }


    public String authenticate(AuthRequest request) {

        User user = getUserByEmail(request.getEmail());
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException();
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        return jwtService.generateToken(userDetails);
    }

    public ArrayList<ResourceDTO> getCollection(String collectionName) {
        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());
        ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
        log.info("service");
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

    public Boolean postResource(PostResourceDTO postResourceDTO) {
        CollectionDTO collectionDTO = postResourceDTO.getCollectionDTO();
        ResourceDTO resourceDTO = postResourceDTO.getResourceDTO();
        String secretToEmbed = postResourceDTO.getSecretToEmbed();
        if(collectionDTO == null) {
            // TODO: bad request
            throw new RuntimeException();
        } else if (!collectionDTO.valid()) {
            // TODO: bad request
           throw new RuntimeException();
        }
        if(resourceDTO == null) {
            // TODO: bad request
            throw new RuntimeException();
        } else if (!resourceDTO.valid()) {
            // TODO: bad request
            throw new RuntimeException();
        }
        if(secretToEmbed == null || secretToEmbed.equals("")) {
            // TODO: bad request
            throw new RuntimeException();
        }

        Collection collection = collectionRepository.getCollectionByNameAndUserId(collectionDTO.getName(), userDetailsService.getCurrentlyLoggedUser().getId());
        if(collection == null) {
            // TODO: create new collection
            ArrayList<Collection> collections = collectionRepository.getCollectionsByUserId(userDetailsService.getCurrentlyLoggedUser().getId());
            for(Collection collection1 : collections) {
                if(collection1.getName().equals(collectionDTO.getName())) {
                    // TODO: bad request
                    throw new RuntimeException();
                }
            }
            Collection newCollection = Collection.builder()
                    .name(collectionDTO.getName().trim())
                    .collectionDescription(collectionDTO.getDescription())
                    .user(userDetailsService.getCurrentlyLoggedUser())
                    .build();
            collectionRepository.save(newCollection);
            sftpService.createCollection(userDetailsService.getCurrentlyLoggedUser().getEmail(), newCollection.getName());
            Resource resource = Resource.builder()
                    .name(resourceDTO.getName().trim())
                    .isSaved(true)
                    .imageType(ImageType.convert(resourceDTO.getType()))
                    .algorithmType(AlgorithmType.convert((resourceDTO.getAlgorithm())))
                    .description(resourceDTO.getDescription())
                    .collection(newCollection)
                    .build();
            resourceRepository.save(resource);
//             TODO: embed secretToEmbed into image and save on SFTP server in the proper collection
            SaveImageLocallyHelper.saveFile(resource, resourceDTO.getImageBytes());
            Secret secret = new Secret(secretToEmbed);
            CoverImage coverImage = new CoverImage();
            coverImage.readImage("currentUserResources/" + "StillCover" + resource.getImageName());
            Algorithm algorithm = new LsbReplacementAlgorithm();
            algorithm.embed(coverImage, secret);
            coverImage.save("currentUserResources/" + resource.getImageName());
            sftpService.uploadFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), newCollection, resource);
//            upAndDownload.uploadFileFromBase64(userDetailsService.getCurrentlyLoggedUser().getEmail(), newCollection, resource, resourceDTO.getImageBytes());
            SaveImageLocallyHelper.remove(resource);
        } else {
            ArrayList<Resource> resources = resourceRepository.getResourcesByCollectionId(collection.getId());
            for(Resource resource : resources) {
                if(resource.getName().equals(resourceDTO.getName())) {
                    // TODO: bad request
                    throw new RuntimeException();
                }
            }
            Resource resource = Resource.builder()
                    .name(resourceDTO.getName())
                    .isSaved(true)
                    .imageType(ImageType.convert(resourceDTO.getType()))
                    .algorithmType(AlgorithmType.convert((resourceDTO.getAlgorithm())))
                    .description(resourceDTO.getDescription())
                    .collection(collection)
                    .build();
            resourceRepository.save(resource);
            // TODO: embed secretToEmbed into image and save on SFTP server in the proper collection
            SaveImageLocallyHelper.saveFile(resource, resourceDTO.getImageBytes());
            Secret secret = new Secret(secretToEmbed);
            CoverImage coverImage = new CoverImage();
            coverImage.readImage("currentUserResources/" + "StillCover" + resource.getImageName());
            Algorithm algorithm = new LsbReplacementAlgorithm();
            algorithm.embed(coverImage, secret);
            coverImage.save("currentUserResources/" + resource.getImageName());
            sftpService.uploadFile(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource);
//            upAndDownload.uploadFileFromBase64(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection, resource, resourceDTO.getImageBytes());
            SaveImageLocallyHelper.remove(resource);
        }

        return true;
    }

    public ExtractedResourceDTO getExtractedResource(String collectionName, String resourceName) {

        Collection collection = this.collectionRepository.getCollectionByNameAndUserId(collectionName, userDetailsService.getCurrentlyLoggedUser().getId());

        if(collection == null) {
            throw new RuntimeException();
        }

        Resource resource = this.resourceRepository.getResourceByNameAndCollectionId(resourceName, collection.getId());

        if(resource == null) {
            throw new RuntimeException();
        }

        ResourceDTO resourceDTO = sftpService.getResource(userDetailsService.getCurrentlyLoggedUser().getEmail(), collection.getName(), resource);

        return ExtractedResourceDTO.builder()
                .imageBytes(resourceDTO.getImageBytes())
                .algorithm(resourceDTO.getAlgorithm())
                .name(resourceDTO.getName())
                .message("asddddddddddddddddddddddddddddddddddddddddddddasdasdiau sdiugaisdo uagsidug asgdou gasougdoa gs iudgoa ugsd ")
                .build();
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

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public User getUser() {
        return this.userDetailsService.getCurrentlyLoggedUser();
    }

}
