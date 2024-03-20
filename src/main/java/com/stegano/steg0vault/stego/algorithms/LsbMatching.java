package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

import java.util.Random;

public class LsbMatching implements Algorithm {
    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        if(coverImage.capacity() < secret.length())
            throw new RuntimeException("The secret is too long to be embedded!");

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                for(int channel = 0; channel < coverImage.channels(); channel++) {
                    if (!secret.hasToEmbed()) {
                        return;
                    }
                    int value = coverImage.get(channel, i, j);
                    int bit = secret.getCurrentBit();
                    if(value % 2 != bit % 2)
                        if(value == 0)
                            coverImage.put(channel, i, j,value + 1);
                        else if(value == 255)
                            coverImage.put(channel, i, j,value - 1);
                        else {
                            Random random = new Random();
                            int randomValue = random.nextInt(100 - 50 + 1) + 50;
                            if(randomValue <= 75)
                                coverImage.put(channel, i, j,value - 1);
                            else
                                coverImage.put(channel, i, j,value + 1);
                        }
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
