package com.stegano.steg0vault.models.enums;

public enum AlgorithmType {
    A_TYPE1, A_TYPE2, A_TYPE3, A_TYPE4;
    public static boolean valid(String algorithm) {
        return algorithm.equals("A_TYPE1") ||
                algorithm.equals("A_TYPE2") ||
                algorithm.equals("A_TYPE3") ||
                algorithm.equals("A_TYPE4");
    }
    public static AlgorithmType convert(String algorithm) {
        switch (algorithm) {
            case "A_TYPE2" -> {
                return A_TYPE2;
            }
            case "A_TYPE3" -> {
                return A_TYPE3;
            }
            case "A_TYPE4" -> {
                return A_TYPE4;
            }
            default -> {
                return A_TYPE1;
            }
        }
    }
}
