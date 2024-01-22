package com.stegano.steg0vault.models.enums;

public enum ImageType {
    PNG, JPG, JPEG;
    public static boolean valid(String type) {
        return type.equals("image/png") ||
                type.equals("image/jpg") ||
                type.equals("image/jpeg");
    }
    public static ImageType convert(String type) {
        switch (type) {
            case "image/jpg" -> {
                return JPG;
            }
            case "image/jpeg" -> {
                return JPEG;
            }
            default -> {
                return PNG;
            }
        }
    }
}
