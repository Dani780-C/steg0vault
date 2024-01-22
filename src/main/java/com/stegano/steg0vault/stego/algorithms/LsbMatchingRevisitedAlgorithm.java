package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

import java.util.Arrays;
import java.util.Random;

public class LsbMatchingRevisitedAlgorithm implements Algorithm {

    // we need to divide all cover image bit stream in blocks of 2 bits
    // we can wrap each pixel into an element like { posX, posY, channel, value }
    //    { 123, 124, 125 }, { 126, 127, 128 }, { 129, 130, 131 }, .....
    //    { 123, 124, 125 }, { 126, 127, 128 }, { 129, 130, 131 }, .....

    // f(A, B) = lsb([A/2] + B)
    @Override
    public void embed(CoverImage coverImage, Secret secret) {

        while(coverImage.hasCapacity() && secret.hasMessageToEmbed()) {
            int[] pixelA = coverImage.getGroup();
            int[] pixelB = coverImage.getGroup();
            int m1 = secret.getCurrentBitOfBitStream();
            int m2 = secret.getCurrentBitOfBitStream();
            if(pixelA[3] % 2 == m1) {
                if(((pixelA[3] / 2 + pixelB[3]) % 2) != m2) {
                    // A' = A
                    // B' = +-1
                    if(pixelB[3] == 255)
                        coverImage.getMatrix().put(
                                pixelB[0], pixelB[1],
                                modifyPixel( pixelB[0], pixelB[1], pixelB[2], coverImage, false)
                        );
                    else
                        coverImage.getMatrix().put(
                                pixelB[0], pixelB[1],
                                modifyPixel( pixelB[0], pixelB[1], pixelB[2], coverImage, true)
                        );
                }
            } else {
                if((((pixelA[3] - 1) / 2 + pixelB[3]) % 2) == m2) {
                    // A' = A - 1
                    coverImage.getMatrix().put(
                            pixelA[0], pixelA[1],
                            modifyPixel( pixelA[0], pixelA[1], pixelA[2], coverImage, false)
                    );
                    // B' = B
                } else {
                    // A' = A + 1
                    coverImage.getMatrix().put(
                            pixelA[0], pixelA[1],
                            modifyPixel( pixelA[0], pixelA[1], pixelA[2], coverImage, true)
                    );
                    // B' = B
                }
            }
        }

    }

    private double[] modifyPixel(int i, int j, int k, CoverImage coverImage, boolean addOrSub) {
        for(int c = 0; c < coverImage.getMatrix().channels(); c++)
            if(c == k) {
                double[] array = coverImage.getMatrix().get(i, j);
                System.out.print("BEFORE [ " + i + " " + j + " " + k + " ] => " + Arrays.toString(array));
                if(addOrSub)
                    if(array[k] == 255)
                        array[k] = 252;
                    else
                        array[k]++;
                else
                    if(array[k] == 0)
                        array[k] = 1;
                    else
                        array[k]--;
                System.out.println(" AFTER [ " + i + " " + j + " " + k + " ] => " + Arrays.toString(array));
                return array;
            }
        return null;
    }

    @Override
    public Secret extract(CoverImage coverImage) {
        Secret secret = new Secret();

        while(coverImage.hasCapacity() && secret.canBuild()) {
            int[] pixelA = coverImage.getGroup();
            int[] pixelB = coverImage.getGroup();
            secret.setBit(pixelA[3] % 2);
            secret.setBit((pixelA[3] / 2 + pixelB[3]) % 2);
        }

        return secret;
    };
}
