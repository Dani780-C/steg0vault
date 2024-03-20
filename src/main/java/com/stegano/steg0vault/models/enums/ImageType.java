package com.stegano.steg0vault.models.enums;

public enum ImageType {
    PNG;
    public static boolean valid(String type) {
        return type.equals("image/png");
    }
    public static ImageType convert(String type) {
        return PNG;
    }
}
