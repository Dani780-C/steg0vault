package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

public interface Algorithm {
    void embed(CoverImage coverImage, Secret secret);
    Secret extract(CoverImage coverImage);
}
