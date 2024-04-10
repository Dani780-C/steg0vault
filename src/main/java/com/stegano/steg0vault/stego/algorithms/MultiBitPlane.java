package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import nu.pattern.OpenCV;

public class MultiBitPlane implements Algorithm {

    @Override
    public void embed(CoverImage coverImage, Secret secret) {

        // TODO: EMBED HEADER 12 BYTES

//        for(int i = 0; i < coverImage.height(); i++)
//            for(int j = 0; j < coverImage.width(); j++) {
//                int m = 0;
//                for (int c = 0; c < 3; c++) {
//                    m += coverImage.get(c, i, j);
//                }
//                m = m / 3;
//                for (int c = 0; c < 3; c++) {
//                    coverImage.put(c, i, j, m);
//                }
//            }

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

        // TODO: convert to CGC

        int ch = 0;
        boolean[][] smooth = new boolean[coverImage.height()][coverImage.width()];

        int window = 3;
        int bit_planes = 3;

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                smooth[i][j] = false;

        while(ch < 3 && secret.hasToEmbed()) {

            for(int i = 0; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_CGC(coverImage.get(ch, i, j)));


            for(int bit_plan = bit_planes - 1; bit_plan >= 0; bit_plan--) {

                int first = 12;
                for(int i = 0; i <= coverImage.height() - window; i++) {
                    for(int j = first; j <= coverImage.width() - window; j++)
                        if(isSmooth(ch, i, j, bit_plan, coverImage)) {
                            for(int i1 = 0; i1 < i + window; i1++)
                                for(int j1 = 0; j1 < j + window; j1++)
                                    smooth[i1][j1] = true;
                        }
                    first = 0;
                }

                // TODO: embed bits in noisy area
                for(int i = 0; i < coverImage.height() && secret.hasToEmbed(); i++)
                    for(int j = 0; j < coverImage.width() && secret.hasToEmbed(); j++)
                        if(!smooth[i][j]) {
                            int val = coverImage.get(ch, i, j);
                            int secretBit = secret.getCurrentBit();
                            int new_val = (val & ~(1 << bit_plan)) | (secretBit << bit_plan);
                            coverImage.put(ch, i, j, new_val);
                        }

            }

            for(int i = 0; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_PBC(coverImage.get(ch, i, j)));

            ch += 1;
        }
    }

    boolean isSmooth(int ch, int i, int j, int bit_plan, CoverImage coverImage) {
        int[][] pixels = new int[3][3];
        for(int i1 = i; i1 < i + 3; i1++)
            for(int j1 = j; j1 < j + 3; j1++) {
                pixels[i1 - i][j1 - j] = (coverImage.get(ch, i, j) >> bit_plan);
                pixels[i1 - i][j1 - j] = pixels[i1 - i][j1 - j] - pixels[0][0];
                if(Math.abs(pixels[i1 - i][j1 - j]) > 1)
                    return false;
            }
        return true;
    }

    public int convert_pixel_to_CGC(int pixel) {
        // b7 b6 b5 b4 b3 b2 b1 b0
        int[] bits = new int[8];
        for(int i = 7; i > -1; i--) { bits[i] = pixel % 2; pixel /= 2; }
        for(int i = 7; i > 0; i--) bits[i] = bits[i] ^ bits[i - 1];
        for(int i = 0; i < 8; i++) pixel = pixel * 2 + bits[i];
        return pixel;
    }

    public int convert_pixel_to_PBC(int pixel) {
        // b7 b6 b5 b4 b3 b2 b1 b0
        int[] bits = new int[8];
        for(int i = 7; i > -1; i--) { bits[i] = pixel % 2; pixel /= 2; }
        for(int i = 1; i < 8; i++) bits[i] = bits[i] ^ bits[i - 1];
        for(int i = 0; i < 8; i++) pixel = pixel * 2 + bits[i];
        return pixel;
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

        int ch = 0;
        boolean[][] smooth = new boolean[coverImage.height()][coverImage.width()];

        int window = 3;
        int bit_planes = 3;

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                smooth[i][j] = false;

        while(ch < 3 && length > secret.getSecret().length()) {

            for(int i = 0; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_CGC(coverImage.get(ch, i, j)));


            for(int bit_plan = bit_planes - 1; bit_plan >= 0; bit_plan--) {

                int first = 12;
                for(int i = 0; i <= coverImage.height() - window; i++) {
                    for(int j = first; j <= coverImage.width() - window; j++)
                        if(isSmooth(ch, i, j, bit_plan, coverImage)) {
                            for(int i1 = 0; i1 < i + window; i1++)
                                for(int j1 = 0; j1 < j + window; j1++)
                                    smooth[i1][j1] = true;
                        }
                    first = 0;
                }

                // TODO: embed bits in noisy area
                for(int i = 0; i < coverImage.height() && length > secret.getSecret().length(); i++)
                    for(int j = 0; j < coverImage.width() && length > secret.getSecret().length(); j++)
                        if(!smooth[i][j]) {
                            int val = coverImage.get(ch, i, j);
                            secret.createSecret((val & (1 << bit_plan)) >> bit_plan);
                        }

            }

            for(int i = 0; i < coverImage.height(); i++)
                for(int j = 0; j < coverImage.width(); j++)
                    coverImage.put(ch, i, j, convert_pixel_to_PBC(coverImage.get(ch, i, j)));

            ch += 1;
        }
        secret.setSecret(secret.getSecret().length() + "." + secret.getSecret());
        return secret;
    }

//    public static void main(String[] args) {
//        OpenCV.loadLocally();
//
//        CoverImage coverImage = new CoverImage();
//        coverImage.readImage("/home/daniel/licenta/steg0vault/currentUserResources/Experiment.png");
//
//        Secret secret = new Secret("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean cursus accumsan dolor, sit amet cursus ligula condimentum ac. Fusce eget aliquam arcu, sit amet tempor risus. Fusce efficitur auctor augue, eu imperdiet tellus varius vel. Donec pretium nunc ipsum, vel dignissim sem facilisis id. Duis luctus nisi id nisl viverra commodo. Mauris euismod lacus nulla, sit amet euismod lectus interdum id. Vivamus non justo ullamcorper, mattis metus nec, iaculis massa. Nunc pretium, augue non pharetra accumsan, libero arcu iaculis erat, in pretium lacus sem non quam. Etiam feugiat nibh nibh, sed accumsan justo imperdiet id.");
//
//
//        Algorithm algorithm = new MultiBitPlane();
//
//        long startTime, stopTime;
//
//        startTime = System.currentTimeMillis();
//        algorithm.embed(coverImage, secret);
//        stopTime = System.currentTimeMillis();
//
//        System.out.println("EMBEDDING DURATION " + String.valueOf((stopTime - startTime) / 1000));
//
//        coverImage.save("/home/daniel/licenta/steg0vault/currentUserResources/TEST_FOR_MULTI_BIT_PLANE_EMBEDDING.png");
//
//        CoverImage coverImage1 = new CoverImage();
//        coverImage1.readImage("/home/daniel/licenta/steg0vault/currentUserResources/TEST_FOR_MULTI_BIT_PLANE_EMBEDDING.png");
//
//        Algorithm algorithm1 = new MultiBitPlane();
//
//        startTime = System.currentTimeMillis();
//        Secret secret1 = algorithm1.extract(coverImage1);
//        stopTime = System.currentTimeMillis();
//
//        System.out.println("EXTRACTION DURATION " + String.valueOf((stopTime - startTime) / 1000));
//
//        System.out.println(secret1.getRealSecret());
//
//    }
}
