package com.stegano.steg0vault.models.enums;

import com.stegano.steg0vault.stego.algorithms.MultiBitPlane;

public enum AlgorithmType {
    LSB_REPLACEMENT, LSB_MATCHING, LSB_MATCHING_REVISITED, BINARY_HAMMING_CODES, RANDOM_PIXEL_SELECTION, MULTI_BIT_PLANE;
    public static boolean valid(String algorithm) {
        return algorithm.equals("LSB_REPLACEMENT") ||
                algorithm.equals("LSB_MATCHING") ||
                algorithm.equals("LSB_MATCHING_REVISITED") ||
                algorithm.equals("BINARY_HAMMING_CODES") ||
                algorithm.equals("RANDOM_PIXEL_SELECTION") ||
                algorithm.equals("MULTI_BIT_PLANE");
    }
    public static AlgorithmType convert(String algorithm) {
        switch (algorithm) {
            case "LSB_MATCHING" -> {
                return LSB_MATCHING;
            }
            case "LSB_MATCHING_REVISITED" -> {
                return LSB_MATCHING_REVISITED;
            }
            case "BINARY_HAMMING_CODES" -> {
                return BINARY_HAMMING_CODES;
            }
            case "RANDOM_PIXEL_SELECTION" -> {
                return RANDOM_PIXEL_SELECTION;
            }
            case "MULTI_BIT_PLANE" -> {
                return MULTI_BIT_PLANE;
            }
            default -> {
                return LSB_REPLACEMENT;
            }
        }
    }
}
