package com.stegano.steg0vault.services;

import com.stegano.steg0vault.exceptions.UserAlreadyExistsException;
import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.*;
import com.stegano.steg0vault.models.enums.RoleType;
import com.stegano.steg0vault.repositories.*;
import com.stegano.steg0vault.sftp.SftpService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final AlgorithmRepository algorithmRepository;
    private final SftpService sftpService;
    private final AuditRepository auditRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final CollectionRepository collectionRepository;
    private final ResourceRepository resourceRepository;

    private final UserService userService;

    public AdminService(UserRepository userRepository, AlgorithmRepository algorithmRepository, SftpService sftpService, SftpService sftpService1, AuditRepository auditRepository, JavaMailSender javaMailSender, PasswordEncoder passwordEncoder, CollectionRepository collectionRepository, ResourceRepository resourceRepository, UserService userService) {
        this.userRepository = userRepository;
        this.algorithmRepository = algorithmRepository;
        this.sftpService = sftpService1;
        this.auditRepository = auditRepository;
        this.javaMailSender = javaMailSender;
        this.passwordEncoder = passwordEncoder;
        this.collectionRepository = collectionRepository;
        this.resourceRepository = resourceRepository;
        this.userService = userService;
    }

    public List<UserInfoDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfoDTO> userInfoDto = new ArrayList<>();
        long pos = 1L;
        for(User user : users) {
            if(user.getRoleType().name().equals(RoleType.USER.name())) {
                userInfoDto.add(
                        UserInfoDTO.builder()
                                .position(pos)
                                .email(user.getEmail())
                                .fullName(user.getLastName() + " " + user.getFirstName())
                                .role(user.getRoleType().name())
                                .id(user.getId())
                                .createdAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                                .deletedAt(user.getDeletedAt() != null ? user.getDeletedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                                .lastActiveDate(user.getModifiedAt() != null ? user.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                                .build()
                );
                pos += 1;
            }
        }
        return userInfoDto;
    }

    public List<AlgDTO> getAllAlgs() {
        List<AlgorithmEntity> algorithmEntities = algorithmRepository.findAll();
        List<AlgDTO> algDTOS = new ArrayList<>();
        long pos = 1L;
        for(AlgorithmEntity algorithm : algorithmEntities) {
            algDTOS.add(
                    AlgDTO.builder()
                            .position(pos)
                            .id(algorithm.getId())
                            .name(algorithm.getName())
                            .createdAt(algorithm.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                            .deletedAt(algorithm.getDeletedAt() != null? algorithm.getDeletedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                            .build()
            );
            pos += 1;
        }
        return algDTOS;
    }

    @Transactional
    public void createAdmin(RegisterRequest request) {
        User usr = userRepository.getUserByEmailAndDeletedAtIsNull(request.getEmail());
        if (usr != null) {
            throw new UserAlreadyExistsException();
        }
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleType(RoleType.ADMIN)
                .build();
        this.userService.createUserSpace(user);
        this.userService.createEncryptionKey(user);
        userRepository.save(user);
        sendMail(user);
    }
    public void sendMail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome Admin " + user.getLastName() + " " + user.getFirstName());
        message.setText("You have been added to admin community by " + userService.getCurrentlyLoggedUser().getLastName()
                + " " + userService.getCurrentlyLoggedUser().getFirstName() + ".\n"
        + "You can connect to your admin account by accessing http://localhost:4200/login");
        javaMailSender.send(message);
    }

    @Transactional
    public void createAlgorithm(String createAlgorithm) {

        if(createAlgorithm == null || createAlgorithm.trim().isEmpty())
            throw new RuntimeException();

        AlgorithmEntity algorithm = algorithmRepository.findAlgorithmEntityByName(createAlgorithm.trim());

        if(algorithm != null)
            throw new RuntimeException();

        AlgorithmEntity algorithm1 = AlgorithmEntity.builder()
                .createdAt(LocalDateTime.now())
                .name(createAlgorithm.trim())
                .build();
        algorithmRepository.save(algorithm1);
    }

    public Details getDetails(Long id) {

        User user = userRepository.getUserById(id);

        if(user == null || user.getRoleType().name().equals("ADMIN"))
            throw new RuntimeException();

        long collNo = user.getCollections().stream().filter(
                coll -> coll.getDeletedAt() == null
        ).count();

        List<Long> resources = user.getCollections().stream().map(
                coll -> coll.getResources().stream().filter(
                        rsc -> rsc.getDeletedAt() == null
                ).count()
        ).toList();

        long sum = 0;
        for (Long l : resources)
            sum += l;

        List<Action> userLogs = auditRepository.getUserLogs(user.getId());
        List<CollectionAction> collectionLogs = auditRepository.getCollectionLogs(user);
        List<CollectionAction> resourceLogs = auditRepository.getResourceLogs(user);

        return Details.builder()
                .numberOfCollections(collNo)
                .numberOfResources(sum)
                .userLogs(userLogs)
                .collectionLogs(collectionLogs)
                .resourceLogs(resourceLogs)
                .build();
    }

    public void markAsInactive(Long id, String banned) {
        User user = userRepository.getUserById(id);

        if(user == null)
            throw new RuntimeException();

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

        if(banned.equals("yes")) {
            sendBannedMail(user);
        }
        else {
            sendInactiveMail(user);
        }
    }

    public void sendBannedMail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account deletion");
        message.setText("Because you uploaded unlawful items, you violated the application's policy, and your account was deleted by the administrator " + userService.getCurrentlyLoggedUser().getLastName()
                + " " + userService.getCurrentlyLoggedUser().getFirstName() + ".\n");
        javaMailSender.send(message);
    }

    public void sendInactiveMail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Account deletion");
        message.setText("The administrator " + userService.getCurrentlyLoggedUser().getLastName()
                + " " + userService.getCurrentlyLoggedUser().getFirstName() + " deactivated your account because your inactive time was too long." + "\n");
        javaMailSender.send(message);
    }

    public List<Long> getAllImages(Long id) {

        User user = userRepository.getUserById(id);
        if(user == null || user.getDeletedAt() != null)
            throw new RuntimeException();

        List<Long> images = new ArrayList<>();

        for(Collection collection : user.getCollections())
            if(collection.getDeletedAt() == null) {
                for(Resource resource : collection.getResources()) {
                    if(resource.getDeletedAt() == null) {
                        images.add(resource.getId());
                    }
                }
            }

        return images;
    }

    public ImageBytes getImage(Long id, Long resourceId) {

        User user = userRepository.getUserById(id);
        if(user == null || user.getDeletedAt() != null)
            throw new RuntimeException();

        Resource resource = resourceRepository.getResourcesById(resourceId);
        if(resource == null || resource.getDeletedAt() != null)
            throw new RuntimeException();

        ResourceDTO resourceDTO = sftpService.getResource(
                user.getEmail(),
                resource.getCollection().getName(),
                resource
        );

        return ImageBytes.builder()
                .imageBytes(resourceDTO.getImageBytes())
                .build();
    }

    public void disableAlg(String algorithmName) {

        AlgorithmEntity algorithm = algorithmRepository.findAlgorithmEntityByName(algorithmName);

        if(algorithm == null)
            throw new RuntimeException();

        algorithm.setDeletedAt(LocalDateTime.now());
        algorithmRepository.save(algorithm);
    }

    public void enableAlg(String algorithmName) {
        AlgorithmEntity algorithm = algorithmRepository.findAlgorithmEntityByName(algorithmName);

        if(algorithm == null)
            throw new RuntimeException();

        algorithm.setDeletedAt(null);
        algorithmRepository.save(algorithm);
    }

    public String getAlgInfo(String algorithmName) {
        AlgorithmEntity algorithm = algorithmRepository.findAlgorithmEntityByName(algorithmName);

        if(algorithm == null)
            throw new RuntimeException();

        Long actives = resourceRepository.countAllByDeletedAtIsNullAndAlgorithmId(algorithm.getId());
        Long total = resourceRepository.countResourcesByAlgorithmId(algorithm.getId());

        return "{ \"actives\": " + actives + ", \"total\": " + total + "}";
    }
}
