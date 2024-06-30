package com.stegano.steg0vault.helpers;

import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.enums.Constants;

import java.io.*;
import java.util.Base64;

public class Helper {

    public static void saveFile(Resource resource, String imageBytes, boolean withHeader) {
        byte[] data = Base64.getDecoder().decode(Helper.removeHeaderBase64(imageBytes));
        if(withHeader) {
            try (OutputStream stream = new FileOutputStream(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName())) {
                stream.write(data);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        else {
            try (OutputStream stream = new FileOutputStream(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName())) {
                stream.write(data);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    public static void removeFile(Resource resource, boolean withHeader) {
        File stillCoverfile;
        if(withHeader)
            stillCoverfile = new File(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + Constants.HEAD_FILE_NAME.getValue() + resource.getImageName());
        else
            stillCoverfile = new File(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
        stillCoverfile.delete();
        if(!resource.getImageName().equals("Steg0Vault_Password.png")) {
            File stegoFile = new File(Constants.LOCAL_DIRECTORY_NAME.getValue() + "/" + resource.getImageName());
            stegoFile.delete();
        }
    }

    public static String removeHeaderBase64(String image) {
        if(image.startsWith("data:image/png;base64,"))
            return image.replace("data:image/png;base64,", "");
        if(image.startsWith("data:image/jpg;base64,"))
            return image.replace("data:image/jpg;base64,", "");
        if(image.startsWith("data:image/jpeg;base64,"))
            return image.replace("data:image/jpeg;base64,", "");
        if(image.startsWith("data:image/bmp;base64,"))
            return image.replace("data:image/bmp;base64,", "");
        if(image.startsWith("data:image/tiff;base64,"))
            return image.replace("data:image/tiff;base64,", "");
        if(image.startsWith("data:image/webp;base64,"))
            return image.replace("data:image/webp;base64,", "");
        if(image.startsWith("data:image/gif;base64,"))
            return image.replace("data:image/gif;base64,", "");
        if(image.startsWith("data:image/avif;base64,"))
            return image.replace("data:image/avif;base64,", "");
        return image;
    }

    public static String addHeaderBase64(String image, Resource resource) {
        if(resource.getImageType().toString().equals("PNG"))
            return "data:image/png;base64," + image;
        if(resource.getImageType().toString().equals("JPG"))
            return "data:image/png;base64," + image;
        if(resource.getImageType().toString().equals("JPEG"))
            return "data:image/jpeg;base64," + image;
        if(resource.getImageType().toString().equals("BMP"))
            return "data:image/bmp;base64," + image;
        if(resource.getImageType().toString().equals("TIFF"))
            return "data:image/tiff;base64," + image;
        if(resource.getImageType().toString().equals("WEBP"))
            return "data:image/webp;base64," + image;
        if(resource.getImageType().toString().equals("GIF"))
            return "data:image/gif;base64," + image;
        if(resource.getImageType().toString().equals("AVIF"))
            return "data:image/avif;base64," + image;
        return image;
    }
}
