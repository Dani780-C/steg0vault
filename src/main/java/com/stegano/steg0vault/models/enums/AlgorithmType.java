package com.stegano.steg0vault.models.enums;

public enum AlgorithmType {
    LSB_REPLACEMENT, LSB_MATCHING, LSB_MATCHING_REVISITED;
    public static boolean valid(String algorithm) {
        return algorithm.equals("LSB_REPLACEMENT") ||
                algorithm.equals("LSB_MATCHING") ||
                algorithm.equals("LSB_MATCHING_REVISITED");
    }
    public static AlgorithmType convert(String algorithm) {
        switch (algorithm) {
            case "LSB_MATCHING" -> {
                return LSB_MATCHING;
            }
            case "LSB_MATCHING_REVISITED" -> {
                return LSB_MATCHING_REVISITED;
            }
            default -> {
                return LSB_REPLACEMENT;
            }
        }
    }
}
