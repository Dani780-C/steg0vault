package com.stegano.steg0vault.sftp;

import com.stegano.steg0vault.exceptions.CreateRemoteCollectionException;
import com.stegano.steg0vault.exceptions.CreateUserSpaceException;
import com.stegano.steg0vault.helpers.Helper;
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
import java.time.format.DateTimeFormatter;
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

    public void uploadEncryptionKey(String localFilename, String userDirectory) {
        SftpSession session = sftpFactory().getSession();
        try {
            File newFile = new File(localFilename);
            InputStream in = new FileInputStream(newFile);
            session.write(in, Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/key.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getEncryptionKey(String userDirectory) {
        SftpSession session = sftpFactory().getSession();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            session.read("upload/" + userDirectory + "/key.txt", outputStream);

            FileWriter myWriter = new FileWriter("./currentUserResources" + "/encryption.txt");
            myWriter.write(outputStream.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ResourceDTO> getResources(String userEmail, String collectionName, ArrayList<Resource> resources) {
        SftpSession session = sftpFactory().getSession();
        ArrayList<ResourceDTO> returnedResources = new ArrayList<>();
        try {
            for (Resource rsc: resources) {
                if(rsc.getDeletedAt() == null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    session.read("upload/" + userEmail + "/" + collectionName + "/" + rsc.getImageName(), outputStream);
                    returnedResources.add(
                            ResourceDTO.builder()
                                    .name(rsc.getName())
                                    .description(rsc.getDescription())
                                    .algorithm(rsc.getAlgorithm().getName())
                                    .type(rsc.getImageType().toString())
                                    .imageBytes("")
                                    .id(rsc.getId())
                                    .createdAt(rsc.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")))
                                    .modifiedAt(rsc.getModifiedAt() != null? rsc.getModifiedAt().format(DateTimeFormatter.ofPattern("yyyy MMM dd")) : null)
                                    .build()
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                    .algorithm(resource.getAlgorithm().getName())
                    .type(resource.getImageType().toString())
                    .imageBytes(Helper.addHeaderBase64(Base64.getEncoder().encodeToString(outputStream.toByteArray()), resource))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String userDirectory, Collection collection, String filename) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.remove(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collection.getName() + "/" + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCollectionName(String userDirectory, String oldName, String newName) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.rename(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + oldName,
                    Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + newName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateResourceName(String userDirectory, String collectionName, String oldName, String newName) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.rename(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collectionName + "/" + oldName,
                    Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collectionName + "/" + newName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCollection(String userDirectory, Collection collection) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.rmdir(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + collection.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUserSpace(String userDirectory) {
        SftpSession session = sftpFactory().getSession();
        try {
            session.remove(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory + "/" + "key.txt");
            session.rmdir(Constants.SFTP_SERVER_REMOTE_DIRECTORY_NAME.getValue() + "/" + userDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}