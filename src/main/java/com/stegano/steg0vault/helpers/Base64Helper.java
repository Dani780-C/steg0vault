package com.stegano.steg0vault.helpers;

import com.stegano.steg0vault.models.entities.Resource;

public class Base64Helper {
    public static String removeHeader(String image) {
        if(image.startsWith("data:image/png;base64,"))
            return image.replace("data:image/png;base64,", "");
        if(image.startsWith("data:image/jpg;base64,"))
            return image.replace("data:image/jpg;base64,", "");
        if(image.startsWith("data:image/jpeg;base64,"))
            return image.replace("data:image/jpeg;base64,", "");
        return image;
    }

    public static String addHeader(String image, Resource resource) {
        if(resource.getImageType().toString().equals("PNG"))
            return "data:image/png;base64," + image;
        if(resource.getImageType().toString().equals("JPG"))
            return "data:image/jpg;base64," + image;
        if(resource.getImageType().toString().equals("JPEG"))
            return "data:image/jpeg;base64," + image;
        return image;
    }
}
