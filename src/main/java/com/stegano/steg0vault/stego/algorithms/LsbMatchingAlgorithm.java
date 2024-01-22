package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

import java.util.Random;

public class LsbMatchingAlgorithm implements Algorithm {
    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        for(int i = 0; i < coverImage.getMatrix().height(); i++)
            for(int j = 0; j < coverImage.getMatrix().width(); j++)
                for(int k = 0; k < coverImage.getMatrix().channels(); k++) {
                    if(!secret.hasMessageToEmbed()) break;
                    int bit = secret.getCurrentBitOfBitStream();
                    if(bit != ((int) coverImage.getMatrix().get(i, j)[k] % 2)) {
                        coverImage.getMatrix().put(
                                i, j,
                                modifyPixel(i, j, k, coverImage)
                        );
                    }
                }
    }

    private double[] modifyPixel(int i, int j, int k, CoverImage coverImage) {
        for(int c = 0; c < coverImage.getMatrix().channels(); c++)
            if(c == k) {
                double[] array = coverImage.getMatrix().get(i, j);
                if(array[k] == 0)
                    array[k]++;
                else if(array[k] == 255)
                    array[k]--;
                else{
                    Random random = new Random();
                    if(random.nextBoolean()) {
                        array[k]++;
                    } else {
                        array[k]--;
                    }
                }
                return array;
            }
        return null;
    }
}
