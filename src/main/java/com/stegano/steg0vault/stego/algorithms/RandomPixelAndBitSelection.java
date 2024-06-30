package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;

import java.util.*;

public class RandomPixelAndBitSelection implements Algorithm {
    @Override
    public void embed(CoverImage coverImage, Secret secret) {

        if((coverImage.width() * coverImage.height()) - 12 < secret.getRealSecret().length())
            throw new RuntimeException();

        Secret newSecret = new Secret();
        newSecret.setSecret(secret.getRealSecret().length() + ".");

        boolean ok = true;
        for(int i = 0; i < coverImage.height() && ok; i++)
            for(int j = 0; j < coverImage.width() && ok; j++)
                for(int channel = 0; channel < coverImage.channels() && ok; channel++) {
                    if (!newSecret.hasToEmbed())
                        ok = false;
                    else
                        coverImage.put(channel, i, j, coverImage.get(channel, i, j) & ~1 | newSecret.getCurrentBit());
                }

        secret.setSecret(secret.getRealSecret());

        Random randomX = new Random(113);
        Random randomY = new Random(226);
        Random randBit = new Random(113);

        boolean[][] matrice = new boolean[coverImage.height()][coverImage.width()];

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                matrice[i][j] = false;

        for(int i = 0; i < secret.getSecret().length(); i++) {

            int x = randomX.nextInt(0, coverImage.height());
            int y;
            if(x == 0)
                y = randomY.nextInt(13, coverImage.width());
            else
                y = randomY.nextInt(0, coverImage.width());

            int left_y = y, right_y = y, x_copy = x;
            boolean ok1 = false;
            if(matrice[x][y]) {

                for(int x1 = x_copy; x1 < coverImage.height() && !ok1; x1++) {
                    for (int y1 = right_y; y1 < coverImage.width() && !ok1; y1++)
                        if (!matrice[x1][y1]) {
                            x = x1;
                            y = y1;
                            ok1 = true;
                        }
                    right_y = 0;
                }

                if(!ok1) {
                    for(int x1 = x_copy; x1 >= 0 && !ok1; x1--) {
                        for (int y1 = left_y; y1 > 0 && !ok1; y1--)
                            if (!matrice[x1][y1]) {
                                x = x1;
                                y = y1;
                                ok1 = true;
                            }
                        left_y = coverImage.width() - 1;
                    }
                }
            }
            else {
                ok1 = true;
            }

            if(!ok1) {
                throw new RuntimeException();
            }

            matrice[x][y] = true;

            double[] values = coverImage.getMatrix().get(x, y);

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int randBitVal = ((int) values[0] & (1 << randomBitLoc)) >> randomBitLoc;
                int bit = secret.getCurrentBit();
                int xor = randBitVal ^ bit;
                values[0] = (double) (((int) values[0] & ~(1 << k)) | (xor << k));
            }

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int randBitVal = ((int) values[1] & (1 << randomBitLoc)) >> randomBitLoc;
                int bit = secret.getCurrentBit();
                int xor = randBitVal ^ bit;
                values[1] = (double) (((int) values[1] & ~(1 << k)) | (xor << k));
            }

            for(int k = 1; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int randBitVal = ((int) values[2] & (1 << randomBitLoc)) >> randomBitLoc;
                int bit = secret.getCurrentBit();
                int xor = randBitVal ^ bit;
                values[2] = (double) (((int) values[2] & ~(1 << k)) | (xor << k));
            }

            coverImage.getMatrix().put(x, y, values);
        }
    }

    @Override
    public Secret extract(CoverImage coverImage) {

        Secret secret1 = new Secret();
        boolean ok = true;
        for (int i = 0; i < coverImage.height() && ok; i++)
            for (int j = 0; j < coverImage.width() && ok; j++)
                for (int channel = 0; channel < coverImage.channels() && ok; channel++) {
                    String s = secret1.getSecret();
                    if (s.matches("[0-9]+\\.")) ok = false;
                    else secret1.createSecret(coverImage.get(channel, i, j) % 2);
                }

        int length = Integer.parseInt(secret1.getSecret().split("\\.")[0]);

        Secret secret = new Secret();

        Random randomX = new Random(113);
        Random randomY = new Random(226);
        Random randBit = new Random(113);

        boolean[][] matrice = new boolean[coverImage.height()][coverImage.width()];

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                matrice[i][j] = false;
        for(int i = 0; i < length; i++) {

            int x = randomX.nextInt(0, coverImage.height());
            int y;
            if(x == 0)
                y = randomY.nextInt(13, coverImage.width());
            else
                y = randomY.nextInt(0, coverImage.width());

            int left_y = y, right_y = y;
            boolean ok1 = false;
            if(matrice[x][y]) {

                for(int x1 = x; x1 < coverImage.height() && !ok1; x1++) {
                    for (int y1 = right_y; y1 < coverImage.width() && !ok1; y1++)
                        if (!matrice[x1][y1]) {
                            x = x1;
                            y = y1;
                            ok1 = true;
                        }
                    right_y = 0;
                }

                if(!ok1) {
                    for(int x1 = x; x1 >= 0 && !ok1; x1--) {
                        for (int y1 = left_y; y1 > 0 && !ok1; y1--)
                            if (!matrice[x1][y1]) {
                                x = x1;
                                y = y1;
                                ok1 = true;
                            }
                        left_y = coverImage.width() - 1;
                    }
                }
            }
            else {
                ok1 = true;
            }

            if(!ok1)
                throw new RuntimeException();

            matrice[x][y] = true;

            double[] values = coverImage.getMatrix().get(x, y);
            int[] intValues = { (int) values[0], (int) values[1], (int) values[2] };

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int bit = (((intValues[0] & (1 << k)) >> k) ^ ((intValues[0] & (1 << randomBitLoc)) >> randomBitLoc));
                secret.createSecret(bit);
            }

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int bit = (((intValues[1] & (1 << k)) >> k) ^ ((intValues[1] & (1 << randomBitLoc)) >> randomBitLoc));
                secret.createSecret(bit);
            }

            for(int k = 1; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int bit = (((intValues[2] & (1 << k)) >> k) ^ ((intValues[2] & (1 << randomBitLoc)) >> randomBitLoc));
                secret.createSecret(bit);
            }

        }
        secret.setSecret(secret.getSecret().length() + "." + secret.getSecret());
        return secret;
    }
}
