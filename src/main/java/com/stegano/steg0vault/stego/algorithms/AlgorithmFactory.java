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
            default -> {
                return new LsbReplacement();
            }
        }
    }
}
