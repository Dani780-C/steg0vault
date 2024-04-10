package com.stegano.steg0vault.models.enums;

public enum ImageType {
//     BMP, PNG, PPM, PNM, TIF, TIFF
    PNG, BMP, TIFF, TIF, PNM, PPM;
//    public static boolean valid(String type) {
//        return type.equals("image/png") || ;
//    }
    public static ImageType convert(String type) {
        return PNG;
    }
}
