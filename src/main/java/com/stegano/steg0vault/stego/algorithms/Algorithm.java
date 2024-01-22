package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

public interface Algorithm {
    void embed(CoverImage coverImage, Secret secret);
    default Secret extract(CoverImage coverImage) {
        Secret secret = new Secret();

        for(int i = 0; i < coverImage.getMatrix().height(); i++)
            for(int j = 0; j < coverImage.getMatrix().width(); j++)
                for(int k = 0; k < coverImage.getMatrix().channels(); k++) {
                    if(!secret.canBuild()) break;
                    secret.setBit((int) coverImage.getMatrix().get(i, j)[k] % 2);
                }

        return secret;
    };
}
