package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

public class LsbMatchingRevisited implements Algorithm {

    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        if(coverImage.capacity() < secret.length())
            throw new RuntimeException("The secret is too long to be embedded!");

        class Group {
            boolean ready = false;
            int i, j, ch, value;
            void set(int i, int j, int ch, int value) {
                this.i = i; this.j = j; this.ch = ch; this.value = value;
                this.ready = true;
            }
        }

        Group group1 = new Group();
        Group group2 = new Group();

        int bit1 = 0, bit2 = 0;
        for (int i = 0; i < coverImage.height(); i++)
            for (int j = 0; j < coverImage.width(); j++)
                for (int ch = 0; ch < coverImage.channels(); ch++) {
                    if(!secret.hasToEmbed()) return;
                    if(!group1.ready) {
                        group1.set(i, j, ch, coverImage.get(ch, i, j));
                        bit1 = secret.getCurrentBit();
                    }
                    else if(!group2.ready) {
                        group2.set(i, j, ch, coverImage.get(ch, i, j));
                        bit2 = secret.getCurrentBit();
                        if(group1.value % 2 == bit1 % 2) {
                            if((group1.value / 2 + group2.value) % 2 != bit2 % 2) {
                                if(group2.value == 0)
                                    coverImage.put(group2.ch, group2.i, group2.j, group2.value + 1);
                                else
                                    coverImage.put(group2.ch, group2.i, group2.j, group2.value - 1);
                            }
                        }
                        else {
                            if(((group1.value - 1) / 2 + group2.value) % 2 == bit2 % 2) {
                                if(group1.value == 0)
                                    coverImage.put(group1.ch, group1.i, group1.j, group1.value + 1);
                                else
                                    coverImage.put(group1.ch, group1.i, group1.j, group1.value - 1);
                            }
                            else if(((group1.value - 1) / 2 + group2.value) % 2 != bit2 % 2) {
                                if(group1.value == 0)
                                    coverImage.put(group1.ch, group1.i, group1.j, group1.value + 3);
                                else if(group1.value == 255)
                                    coverImage.put(group1.ch, group1.i, group1.j, group1.value - 3);
                                else
                                    coverImage.put(group1.ch, group1.i, group1.j, group1.value + 1);
                            }
                        }
                        group1.ready = false;
                        group2.ready = false;
                    }
                }
    }

    @Override
    public Secret extract(CoverImage coverImage) {
        Secret secret = new Secret();
        class Group {
            boolean ready = false;
            int i, j, ch, value;
            void set(int i, int j, int ch, int value) {
                this.i = i; this.j = j; this.ch = ch; this.value = value;
                this.ready = true;
            }
        }

        Group group1 = new Group();
        Group group2 = new Group();

        int bit1 = 0, bit2 = 0;
        for (int i = 0; i < coverImage.height(); i++)
            for (int j = 0; j < coverImage.width(); j++)
                for (int ch = 0; ch < coverImage.channels(); ch++) {
                    if(secret.canCreate()) return secret;
                    if(!group1.ready) {
                        group1.set(i, j, ch, coverImage.get(ch, i, j));
                        bit1 = group1.value % 2;
                    }
                    else if(!group2.ready) {
                        group2.set(i, j, ch, coverImage.get(ch, i, j));
                        bit2 = (group1.value / 2 + group2.value) % 2;
                        secret.createSecret(bit1);
                        secret.createSecret(bit2);
                        group1.ready = false;
                        group2.ready = false;
                    }
                }
        return secret;
    }
}
