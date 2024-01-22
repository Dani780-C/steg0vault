package com.stegano.steg0vault.sftp;

import com.stegano.steg0vault.helpers.Base64Helper;
import com.stegano.steg0vault.models.DTOs.ResourceDTO;
import com.stegano.steg0vault.models.entities.Collection;
import com.stegano.steg0vault.models.entities.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

@Service
@Slf4j
public class UpAndDownload {

    private DefaultSftpSessionFactory sftpFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost("0.0.0.0");
        factory.setPort(22);
        factory.setAllowUnknownKeys(true);
        factory.setUser("dani");
        factory.setPassword("dani");
        return factory;
    }

    public void sftpMkdir(String emailAsDirectory) {

        SftpSession session = sftpFactory().getSession();
        try {
            session.mkdir("upload/" + emailAsDirectory);
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createCollection(String directory, String collection) {

        SftpSession session = sftpFactory().getSession();
        try {
            session.mkdir("upload/" + directory + "/" + collection);
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void uploadFile(String userDirectory, Collection collection, Resource resource) {
        SftpSession session = sftpFactory().getSession();
        try {
            File newFile = new File("./currentUserResources/" + resource.getImageName());
            InputStream in = new FileInputStream(newFile);
            session.write(in, "upload/" + userDirectory + "/" + collection.getName() + "/" + resource.getImageName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: repair upload file
//    public void uploadFileFromBase64(String userDirectory, Collection collection, Resource resource, String imageBytes) {
//        SftpSession session = sftpFactory().getSession();
//        try {
////            System.out.println(imageBytes);
//            byte[] data = Base64.getDecoder().decode(Base64Helper.removeHeader(imageBytes).getBytes());
////            System.out.println("base 64 decode yes");
////            File file = new File( "./currentUserResources/" + resource.getImageName() );
////            System.out.println("create file yes");
////            FileUtils.writeByteArrayToFile( file, Base64Helper.removeHeader(imageBytes).getBytes() );
////            System.out.println("write file yes");
////            try( OutputStream stream = new FileOutputStream("./currentUserResources/" + resource.getImageName()) )
////            {
////                stream.write(data);
////            }
////            catch (Exception e)
////            {
////                System.err.println("Couldn't write to file...");
////            }
////            File newFile = new File("./currentUserResources/" + resource.getImageName());
////            InputStream in = new FileInputStream(newFile);
//            InputStream in = new ByteArrayInputStream(data);
//            session.write(in, "upload/" + userDirectory + "/" + collection.getName() + "/" + resource.getImageName());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
}