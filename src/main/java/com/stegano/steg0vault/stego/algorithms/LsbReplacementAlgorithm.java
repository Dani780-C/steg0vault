package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

import java.util.Arrays;

public class LsbReplacementAlgorithm implements Algorithm {
    @Override
    public void embed(CoverImage coverImage, Secret secret) {

        for(int i = 0; i < coverImage.getMatrix().height(); i++)
            for(int j = 0; j < coverImage.getMatrix().width(); j++)
                for(int k = 0; k < coverImage.getMatrix().channels(); k++) {
                    if(!secret.hasMessageToEmbed()) break;
                    coverImage.getMatrix().put(
                            i, j,
                            modifyPixel(i, j, k, coverImage, secret.getCurrentBitOfBitStream())
                    );
                }
    }

    private double[] modifyPixel(int i, int j, int k, CoverImage coverImage, int bit) {
        for(int c = 0; c < coverImage.getMatrix().channels(); c++)
            if(c == k) {
                double[] array = coverImage.getMatrix().get(i, j);
                System.out.print("BEFORE [ " + i + " " + j + " " + k + " ] => " + Arrays.toString(array));
                array[k] = ((int) array[k]) & ~1 | bit;
                System.out.println(" AFTER [ " + i + " " + j + " " + k + " ] => " + Arrays.toString(array) + " BIT " + bit);
                return array;
            }
        return null;
    }

}
