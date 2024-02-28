package com.stegano.steg0vault.sftp;

import com.stegano.steg0vault.exceptions.CreateRemoteCollectionException;
import com.stegano.steg0vault.exceptions.CreateUserSpaceException;
import com.stegano.steg0vault.helpers.Base64Helper;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.enums.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

@Service
@Slf4j
public class SftpService {

    @Value("${sftpHost}")
    private String sftpHost;
    @Value("${sftpPort}")
    private int sftpPort;
    @Value("${sftpUser}")
    private String sftpUser;
    @Value("${sftpPass}")
    private String sftpPassword;
    private DefaultSftpSessionFactory sftpFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setAllowUnknownKeys(true);
        factory.setUser(sftpUser);
        factory.setPassword(sftpPassword);
        return factory;
    }

    public void createUserSpace(String userSpaceName) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.mkdir(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userSpaceName);
            session.close();
        } catch (IOException e) {
            throw new CreateUserSpaceException();
        }
    }

    public void createCollection(String userDirectory, String collection) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.mkdir(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collection);
            session.close();
        } catch (IOException e) {
            throw new CreateRemoteCollectionException();
        }
    }

    public void uploadFile(String userDirectory, Collection collection, Resource resource) {
        SftpSession session = sftpFactory().getSession();
        try {
            File newFile = new File("./" + Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
            InputStream in = new FileInputStream(newFile);
            session.write(in, Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collection.getName() + "/" + resource.getImageName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: reconsider the base64 encoding
    public ArrayList<ResourceDTO> getResources(String userEmail, String collectionName, ArrayList<Resource> resources) {
        SftpSession session = sftpFactory().getSession();
        ArrayList<ResourceDTO> returnedResources = new ArrayList<>();
        try {
            for (Resource rsc: resources) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                session.read("upload/" + userEmail + "/" + collectionName + "/" + rsc.getImageName(), outputStream);
                returnedResources.add(
                        ResourceDTO.builder()
                                .name(rsc.getName())
                                .description(rsc.getDescription())
                                .algorithm(rsc.getAlgorithmType().toString())
                                .type(rsc.getImageType().toString())
                                .isSaved(rsc.isSaved())
                                .imageBytes(Base64Helper.addHeader(Base64.getEncoder().encodeToString(outputStream.toByteArray()), rsc))
                                .build()
                );
//                System.out.println(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
            }
        } catch (IOException e) {
            log.info("up and down error");
            throw new RuntimeException(e);
        }
        log.info("up and down success");
        return returnedResources;
    }


    public ResourceDTO getResource(String userDirectory, String collectionName, Resource resource) {
        SftpSession session = sftpFactory().getSession();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            session.read(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collectionName + "/" + resource.getImageName(), outputStream);
            return ResourceDTO.builder()
                    .name(resource.getName())
                    .description(resource.getDescription())
                    .algorithm(resource.getAlgorithmType().toString())
                    .type(resource.getImageType().toString())
                    .isSaved(resource.isSaved())
                    .imageBytes(Base64Helper.addHeader(Base64.getEncoder().encodeToString(outputStream.toByteArray()), resource))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}