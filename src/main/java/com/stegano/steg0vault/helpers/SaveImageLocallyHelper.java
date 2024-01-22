package com.stegano.steg0vault.helpers;

import com.stegano.steg0vault.models.entities.Resource;

import java.io.*;
import java.util.Base64;

public class SaveImageLocallyHelper {

    public static void saveFile(Resource resource, String imageBytes) {
//        File file = new File(resource.getImageName());
        byte[] data = Base64.getDecoder().decode(Base64Helper.removeHeader(imageBytes).getBytes());
        try (OutputStream stream = new FileOutputStream("currentUserResources/" + "StillCover" + resource.getImageName())) {
            stream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void remove(Resource resource) {
        File stillCoverfile = new File("currentUserResources/" + "StillCover" + resource.getImageName());
        stillCoverfile.delete();
        File stegoFile = new File("currentUserResources/" + resource.getImageName());
        stegoFile.delete();
    }

}
