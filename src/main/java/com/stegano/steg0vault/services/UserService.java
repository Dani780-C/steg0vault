package com.stegano.steg0vault.services;

import com.stegano.steg0vault.exceptions.UserAlreadyExistsException;
import com.stegano.steg0vault.exceptions.UserNotFoundException;
import com.stegano.steg0vault.helpers.Helper;
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
import com.stegano.steg0vault.sftp.SftpService;
import com.stegano.steg0vault.stego.algorithms.Algorithm;
import com.stegano.steg0vault.stego.algorithms.LsbMatchingRevisited;
import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final SftpService sftpService;

    public UserService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            ResourceRepository resourceRepository,
            CollectionRepository collectionRepository,
            SftpService sftpService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.resourceRepository = resourceRepository;
        this.collectionRepository = collectionRepository;
        this.sftpService = sftpService;
    }

    @Transactional
    public String register(RegisterRequest request) {
        User usr = getUserByEmail(request.getEmail());
        if (usr != null) {
            throw new UserAlreadyExistsException();
        }
        // TODO: validate password and email format
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleType(RoleType.USER)
                .build();
        this.createUserSpace(user);
//        Collection collection = this.createDefaultCollection(user);
//        this.createDefaultResource(user, collection);
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
                .description(Constants.DEFAULT_RESOURCE_DESCRIPTION.getValue())
                .imageType(ImageType.PNG)
                .algorithmType(AlgorithmType.LSB_REPLACEMENT)
                .collection(collection)
                .build();
        // TODO: 1. embed the password in the default resource
        sftpService.uploadFile(user.getEmail(), collection, resource);
        resourceRepository.save(resource);
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

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public User getCurrentlyLoggedUser() {
        return this.userDetailsService.getCurrentlyLoggedUser();
    }

}
