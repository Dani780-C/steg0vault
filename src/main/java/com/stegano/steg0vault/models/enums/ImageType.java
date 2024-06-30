package com.stegano.steg0vault.models.enums;

public enum ImageType {
    PNG, BMP, TIFF, TIF, PNM, PPM, WEBP, GIF, AVIF;

    public static ImageType convert(String type) {
        switch (type.trim()) {
            case "image/tiff" -> {
                return PNG;
            }
            case "image/avif" -> {
                return AVIF;
            }
            case "image/gif" -> {
                return GIF;
            }
            case "image/webp" -> {
                return WEBP;
            }
            case "image/tif" -> {
                return TIF;
            }
            case "image/bmp" -> {
                return PNG;
            }
            case "image/pnm" -> {
                return PNM;
            }
            case "image/ppm" -> {
                return PPM;
            }
            default -> {
                return PNG;
            }
        }
    }
}
