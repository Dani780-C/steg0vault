package com.stegano.steg0vault.services;

import com.stegano.steg0vault.exceptions.UserAlreadyExistsException;
import com.stegano.steg0vault.exceptions.UserNotFoundException;
import com.stegano.steg0vault.models.DTOs.ChangePassword;
import com.stegano.steg0vault.models.DTOs.ResetPassword;
import com.stegano.steg0vault.models.DTOs.UpdateUser;
import com.stegano.steg0vault.models.DTOs.UserDTO;
import com.stegano.steg0vault.models.entities.*;
import com.stegano.steg0vault.models.enums.Constants;
import com.stegano.steg0vault.models.enums.ImageType;
import com.stegano.steg0vault.models.enums.RoleType;
import com.stegano.steg0vault.repositories.CollectionRepository;
import com.stegano.steg0vault.repositories.ResourceRepository;
import com.stegano.steg0vault.repositories.UserRepository;
import com.stegano.steg0vault.security.Encryption.AES;
import com.stegano.steg0vault.security.JwtService;
import com.stegano.steg0vault.sftp.SftpService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
    private final JavaMailSender javaMailSender;

    public UserService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            ResourceRepository resourceRepository,
            CollectionRepository collectionRepository,
            SftpService sftpService, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.resourceRepository = resourceRepository;
        this.collectionRepository = collectionRepository;
        this.sftpService = sftpService;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public String register(RegisterRequest request) {
        User usr = userRepository.getUserByEmailAndDeletedAtIsNull(request.getEmail());
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
        this.createEncryptionKey(user);
        return jwtService.generateToken(user);
    }

    public void createAdmin() {
        User user = userRepository.getUserByEmail("admin@admin.com");
        if(user == null) {
            User user1 = User.builder()
                    .email("admin@admin.com")
                    .firstName("Admin")
                    .lastName("Super")
                    .password(passwordEncoder.encode("W3wavkjd@"))
                    .roleType(RoleType.ADMIN)
                    .build();
            userRepository.save(user1);
            this.createUserSpace(user1);
            this.createEncryptionKey(user1);
        }
    }

    public void createUserSpace(User user) {
        sftpService.createUserSpace(user.getEmail());
        userRepository.save(user);
    }

    public void createEncryptionKey(User user) {
        AES aes = new AES();
        String key = aes.createEncryptionKey();
        File myObj = new File("./currentUserResources" + "/encryption.txt");
        try {
            FileWriter myWriter = new FileWriter("./currentUserResources" + "/encryption.txt");
            myWriter.write(key);
            myWriter.close();
            // TODO: upload on sftp server
            sftpService.uploadEncryptionKey(
                    "./currentUserResources" + "/" + "encryption.txt",
                    user.getEmail()
            );
        }
        catch(Exception e) {
            log.error("Cannot create encryption key");
        }
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
                .collection(collection)
                .build();
        // TODO: 1. embed the password in the default resource
        sftpService.uploadFile(user.getEmail(), collection, resource);
        resourceRepository.save(resource);
    }

    public String authenticate(AuthRequest request) {
        User user = userRepository.getUserByEmailAndDeletedAtIsNull(request.getEmail());
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
        sftpService.getEncryptionKey(user.getEmail());
        return jwtService.generateToken(userDetails);
    }

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public UserDTO  getUserInfo() {
        User user = userDetailsService.getCurrentlyLoggedUser();
        return UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                .modifiedAt(user.getModifiedAt() != null? user.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                .role(user.getRoleType().name())
                .build();
    }

    public User getCurrentlyLoggedUser() {
        return this.userDetailsService.getCurrentlyLoggedUser();
    }

    public void forgotPassword(String mail) {
        if(mail == null)
            throw new RuntimeException();

        User user = userRepository.getUserByEmailAndDeletedAtIsNull(mail);

        if(user == null)
            throw new RuntimeException();

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        sendMail(user.getEmail(), resetUrl);
    }

    public void sendMail(String email, String resetUrl) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request -- StegoVault");
            message.setText("To reset your password, click the link below:\n" + resetUrl);
            javaMailSender.send(message);
    }

    public void resetPassword(ResetPassword resetPassword) {
        if(resetPassword == null)
            throw new RuntimeException();

        if(resetPassword.getNewPassword() == null || resetPassword.getRetypedNewPassword() == null || resetPassword.getToken() == null)
            throw new RuntimeException();

        if(!resetPassword.getNewPassword().equals(resetPassword.getRetypedNewPassword()))
            throw new RuntimeException();

        User userByToken = userRepository.getUserByResetPasswordTokenAndDeletedAtIsNull(resetPassword.getToken());

        if(userByToken == null)
            throw new RuntimeException();

        if(userByToken.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException();

        userByToken.setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
        userByToken.setResetPasswordToken(null);
        userByToken.setResetPasswordTokenExpiresAt(null);

        userRepository.save(userByToken);
    }

    public void changePassword(ChangePassword changePassword) {

        if(changePassword == null || changePassword.getNewPassword() == null || changePassword.getRetypedNewPassword() == null)
            throw new RuntimeException();

        if(!changePassword.getNewPassword().equals(changePassword.getRetypedNewPassword()))
            throw new RuntimeException();

        User user = userRepository.getUserByEmailAndDeletedAtIsNull(userDetailsService.getCurrentlyLoggedUser().getEmail());

        if(user == null)
            throw new RuntimeException();

        user.setPassword(passwordEncoder.encode(changePassword.getNewPassword()));
        userRepository.save(user);
    }

    public UserDTO updateUser(UpdateUser updateUser) {
        if(updateUser == null || updateUser.getFirstName() == null || updateUser.getLastName() == null)
            throw new RuntimeException();

        if(updateUser.getLastName().isEmpty() || updateUser.getFirstName().isEmpty())
            throw new RuntimeException();

        User user = userRepository.getUserByEmailAndDeletedAtIsNull(
                userDetailsService.getCurrentlyLoggedUser().getEmail()
        );

        user.setLastName(updateUser.getLastName());
        user.setFirstName(updateUser.getFirstName());

        userRepository.save(user);
        return UserDTO.builder()
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .build();
    }

    public void deleteAccount() {
        User user = userDetailsService.getCurrentlyLoggedUser();

        List<Collection> collectionList = user.getCollections().stream().filter(
                coll -> coll.getDeletedAt() == null
        ).toList();

        for(Collection collection : collectionList) {
            List<Resource> resources = collection.getResources().stream().filter(
                    rsc -> rsc.getDeletedAt() == null
            ).toList();
            for(Resource rsc : resources) {
                sftpService.deleteFile(
                        user.getEmail(),
                        collection,
                        rsc.getImageName()
                );
                rsc.setDeletedAt(LocalDateTime.now());
                resourceRepository.save(rsc);
            }
            sftpService.deleteCollection(
                    user.getEmail(),
                    collection
            );
            collection.setDeletedAt(LocalDateTime.now());
            collectionRepository.save(collection);
        }
        user.setDeletedAt(LocalDateTime.now());
        sftpService.deleteUserSpace(user.getEmail());
        userRepository.save(user);
    }
}
