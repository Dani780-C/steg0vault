package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

public class LsbReplacement implements Algorithm {


    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        if(coverImage.capacity() < secret.length())
            throw new RuntimeException("The secret is too long to be embedded!");

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                for(int channel = 0; channel < coverImage.channels(); channel++) {
                    if (!secret.hasToEmbed()) return;
                    coverImage.put(channel, i, j, coverImage.get(channel, i, j) & ~1 | secret.getCurrentBit());
                }
    }

    @Override
    public Secret extract(CoverImage coverImage) {
        Secret secret = new Secret();
        for (int i = 0; i < coverImage.height(); i++)
            for (int j = 0; j < coverImage.width(); j++)
                for (int channel = 0; channel < coverImage.channels(); channel++) {
                    if (secret.canCreate()) return secret;
                    secret.createSecret(coverImage.get(channel, i, j) % 2);
                }
        return secret;
    }
}
