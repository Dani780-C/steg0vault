package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

import java.util.Arrays;

public class LsbReplacementAlgorithm implements Algorithm {


    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        if(coverImage.capacity() < secret.length())
            throw new RuntimeException("The secret is too long to be embedded!");

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                for(int channel = 0; channel < coverImage.channelsToBeUsed(); channel++) {
                    if (!secret.hasToEmbed()) {
                        coverImage.save("currentUserResources/STEG00_LSB.png");
                        return;
                    }
                    coverImage.put(channel, i, j, coverImage.get(channel, i, j) & ~1 | secret.getCurrentBit());
                }
        coverImage.save("currentUserResources/STEG00_LSB.png");
    }

    @Override
    public Secret extract(CoverImage coverImage) {
        Secret secret = new Secret();
        for (int i = 0; i < coverImage.height(); i++)
            for (int j = 0; j < coverImage.width(); j++)
                for (int channel = 0; channel < coverImage.channelsToBeUsed(); channel++) {
                    if (!secret.canExtract()) return secret;
                    secret.createSecret(coverImage.get(channel, i, j) % 2);
                }
        return secret;
    }
}
