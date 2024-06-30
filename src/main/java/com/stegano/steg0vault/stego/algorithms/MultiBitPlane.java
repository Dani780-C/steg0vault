package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MultiBitPlane implements Algorithm {

    @Override
    public void embed(CoverImage coverImage, Secret secret) {

        Secret newSecret = new Secret();
        newSecret.setSecret(secret.getRealSecret().length() + ".");

        boolean ok = true;
        for(int i = 0; i < coverImage.height() && ok; i++)
            for(int j = 0; j < coverImage.width() && ok; j++)
                for(int channel = 0; channel < 3 && ok; channel++) {
                    if (!newSecret.hasToEmbed())
                        ok = false;
                    else {
                        coverImage.put(channel, i, j, coverImage.get(channel, i, j) & ~1 | newSecret.getCurrentBit());
                    }
                }

        secret.setSecret(secret.getRealSecret());

        int ch = 0;
        int[][] smooth = new int[coverImage.height()][coverImage.width()];

        int window = 3;
        int bit_planes = 3;

        while(ch < 3 && secret.hasToEmbed()) {

            for(int i = 1; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_CGC(coverImage.get(ch, i, j)));


            for(int i = 0; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    smooth[i][j] = -1;

            for(int bit_plan = bit_planes - 1; bit_plan >= 0; bit_plan--) {

                for(int i = 1; i < coverImage.height(); i++) {
                    for(int j = 0; j < coverImage.width(); j++)
                        if(smooth[i][j] == -1 && isSmooth(ch, i, j, bit_plan, coverImage)) {
                            for(int i1 = i; (i1 < i + window) && (i1 < coverImage.height()); i1++)
                                for(int j1 = j; (j1 < j + window) && (j1 < coverImage.width()); j1++)
                                    smooth[i1][j1] = bit_plan;
                        }
                }

                // TODO: embed bits in noisy area
                for(int i = 1; i < coverImage.height() && secret.hasToEmbed(); i++) {
                    for (int j = 0; j < coverImage.width() && secret.hasToEmbed(); j++) {
                        if (smooth[i][j] == -1) {

                            smooth[i][j] = -2;
                            int val = coverImage.get(ch, i, j);
                            int secretBit = secret.getCurrentBit();

                            int new_val = (val & ~(1 << bit_plan)) | (secretBit << bit_plan);
                            coverImage.put(ch, i, j, new_val);

                            int newValTrunc = new_val >> bit_plan;
                            int rest = new_val % ((int) Math.pow(2, bit_plan));

                            List<Integer> values = getNeighbourValues(i, j, bit_plan, window, coverImage, ch);

                            boolean ok1 = true;
                            for (Integer integer : values)
                                if (Math.abs(integer - newValTrunc) == 0) {
                                    ok1 = false;
                                    break;
                                }

                            if (!ok1) {
                                int candidate1;
                                for (candidate1 = newValTrunc; candidate1 >= 0; candidate1 -= 2) {
                                    if ((candidate1 % 2) == (newValTrunc % 2)) {
                                        boolean ok2 = true;
                                        for (Integer v : values) {
                                            if (Math.abs(v - candidate1) == 0) {
                                                ok2 = false;
                                                break;
                                            }
                                        }
                                        if (ok2) break;
                                    }
                                }

                                int candidate2;
                                for (candidate2 = newValTrunc; candidate2 <= ((int) (Math.pow(2, 8 - bit_plan)) - 1); candidate2 += 2) {
                                    if ((candidate2 % 2) == (newValTrunc % 2)) {
                                        boolean ok2 = true;
                                        for (Integer v : values) {
                                            if (Math.abs(v - candidate2) == 0) {
                                                ok2 = false;
                                                break;
                                            }
                                        }
                                        if (ok2) break;
                                    }
                                }

                                if(candidate1 == newValTrunc) {
                                    continue;
                                }
                                else if (Math.abs(candidate1 - newValTrunc) <= Math.abs(candidate2 - newValTrunc) && candidate1 >= 0) {
                                    coverImage.put(ch, i, j, (candidate1 << bit_plan) + rest);
                                } else if (candidate2 <= ((int) (Math.pow(2, 8 - bit_plan)) - 1)) {
                                    coverImage.put(ch, i, j, (candidate2 << bit_plan) + rest);
                                } else if(candidate1 >= 0) {
                                    coverImage.put(ch, i, j, (candidate1 << bit_plan) + rest);
                                }
                            }
                        }

                    }
                }
            }
//
            for(int i = 1; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_PBC(coverImage.get(ch, i, j)));

            ch += 1;
        }

        if(secret.hasToEmbed()) {
            log.error("Cannot embed whole message!");
            throw new RuntimeException("");
        }
    }

    List<Integer> getNeighbourValues(int i, int j, int bit_plan, int window, CoverImage coverImage, int ch) {
        List<Integer> values = new ArrayList<>();

        for(int i1 = i; i1 < i + window && i1 < coverImage.height(); i1++)
            for(int j1 = j; j1 < j + window && j1 < coverImage.width(); j1++) {
                if(j1 != j || i1 != i) values.add(coverImage.get(ch, i1, j1) >> bit_plan);
            }

        for(int j1 = j; j1 >= 0 && j1 > (j - window) && i > 0; j1--)
            if(j1 != j)
                values.add(coverImage.get(ch, i, j1) >> bit_plan);

        i--;
        for(int j1 = j; j1 >= 0 && j1 > (j - window) && i > 0; j1--)
            values.add(coverImage.get(ch, i, j1) >> bit_plan);

        i--;
        for(int j1 = j; j1 >= 0 && j1 > (j - window) && i > 0; j1--)
            values.add(coverImage.get(ch, i, j1) >> bit_plan);

        return values;
    }

    boolean isSmooth(int ch, int i, int j, int bit_plan, CoverImage coverImage) {
        int window = 3;
        int[][] pixels = new int[window][window];
        int pivot = coverImage.get(ch, i, j) >> bit_plan;
        for(int i1 = i; (i1 < i + window) && (i1 < coverImage.height()); i1++)
            for(int j1 = j; (j1 < j + window) && (j1 < coverImage.width()); j1++) {
                pixels[i1 - i][j1 - j] = (coverImage.get(ch, i1, j1) >> bit_plan);
                pixels[i1 - i][j1 - j] = pixels[i1 - i][j1 - j] - pivot;
                if(Math.abs(pixels[i1 - i][j1 - j]) > 0) {
                    return false;
                }
            }
        return true;
    }

    public int convert_pixel_to_CGC(int pixel) {
//        // b7 b6 b5 b4 b3 b2 b1 b0
//        int[] bits = new int[8];
//        for(int i = 7; i > -1; i--) { bits[i] = pixel % 2; pixel /= 2; }
//        for(int i = 7; i > 0; i--) bits[i] = bits[i] ^ bits[i - 1];
//        for(int i = 0; i < 8; i++) pixel = pixel * 2 + bits[i];
//        return pixel;

        // b7 b6 b5 b4 b3 < b2 b1 b0 >
        int[] bits = new int[8];
        for(int i = 7; i > -1; i--) { bits[i] = pixel % 2; pixel /= 2; }
        for(int i = 7; i > 5; i--) bits[i] = bits[i] ^ bits[i - 1];
        for(int i = 0; i < 8; i++) pixel = pixel * 2 + bits[i];
        return pixel;
    }

    public int convert_pixel_to_PBC(int pixel) {
//        // b7 b6 b5 b4 b3 b2 b1 b0
//        int[] bits = new int[8];
//        for(int i = 7; i > -1; i--) { bits[i] = pixel % 2; pixel /= 2; }
//        for(int i = 1; i < 8; i++) bits[i] = bits[i] ^ bits[i - 1];
//        for(int i = 0; i < 8; i++) pixel = pixel * 2 + bits[i];
//        return pixel;
        // b7 b6 b5 b4 b3 b2 b1 b0
        int[] bits = new int[8];
        for(int i = 7; i > -1; i--) { bits[i] = pixel % 2; pixel /= 2; }
        for(int i = 6; i < 8; i++) bits[i] = bits[i] ^ bits[i - 1];
        for(int i = 0; i < 8; i++) pixel = pixel * 2 + bits[i];
        return pixel;
    }

    @Override
    public Secret extract(CoverImage coverImage) {

        Secret secret1 = new Secret();
        boolean ok = true;
        for (int i = 0; i < coverImage.height() && ok; i++)
            for (int j = 0; j < coverImage.width() && ok; j++)
                for (int channel = 0; channel < 3 && ok; channel++) {
                    String s = secret1.getSecret();
                    if (s.matches("[0-9]+\\.")) ok = false;
                    else {
                        secret1.createSecret(coverImage.get(channel, i, j) % 2);
                    }
                }

        int length = Integer.parseInt(secret1.getSecret().split("\\.")[0]);
        Secret secret = new Secret();

        int ch = 0;
        int[][] smooth = new int[coverImage.height()][coverImage.width()];

        int window = 3;
        int bit_planes = 3;

        while(ch < 3 && length > secret.getSecret().length()) {

            for(int i = 1; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_CGC(coverImage.get(ch, i, j)));

            for(int i = 0; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    smooth[i][j] = -1;

            for(int bit_plan = bit_planes - 1; bit_plan >= 0; bit_plan--) {

                for(int i = 1; i < coverImage.height() ; i++) {
                    for(int j = 0; j < coverImage.width() ; j++)
                        if(smooth[i][j] == -1 && isSmooth(ch, i, j, bit_plan, coverImage)) {
                            for (int i1 = i; (i1 < i + window) && (i1 < coverImage.height()); i1++)
                                for (int j1 = j; (j1 < j + window) && (j1 < coverImage.width()); j1++)
                                    smooth[i1][j1] = bit_plan;
                        }
                }

                for(int i = 1; i < coverImage.height() && length > secret.getSecret().length(); i++) {
                    for (int j = 0; j < coverImage.width() && length > secret.getSecret().length(); j++)
                        if (smooth[i][j] == -1) {
                            int val = coverImage.get(ch, i, j);
                            secret.createSecret((val & (1 << bit_plan)) >> bit_plan);
                            smooth[i][j] = -2;
                        }
                }

            }

            for(int i = 1; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_PBC(coverImage.get(ch, i, j)));

            ch += 1;
        }

        secret.setSecret(secret.getSecret().length() + "." + secret.getSecret());
        return secret;
    }
}
