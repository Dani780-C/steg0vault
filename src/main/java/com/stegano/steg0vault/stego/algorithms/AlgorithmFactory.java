package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.models.enums.AlgorithmType;

public class AlgorithmFactory {

    public static Algorithm createAlgorithm(AlgorithmType algorithmType) {
        switch (algorithmType) {
            case LSB_MATCHING -> {
                return new LsbMatching();
            }
            case LSB_MATCHING_REVISITED -> {
                return new LsbMatchingRevisited();
            }
            case BINARY_HAMMING_CODES -> {
                return new BinaryHammingCodes();
            }
            case RANDOM_PIXEL_SELECTION -> {
                return new RandomPixelAndBitSelection();
            }
            case MULTI_BIT_PLANE -> {
                return new MultiBitPlane();
            }
            default -> {
                return new LsbReplacement();
            }
        }
    }
}
