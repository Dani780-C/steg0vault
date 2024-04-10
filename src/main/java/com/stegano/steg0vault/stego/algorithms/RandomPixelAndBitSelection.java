package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import lombok.Getter;
import nu.pattern.OpenCV;
import org.springframework.integration.json.JsonPathUtils;


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
        System.out.println(secret.getSecret().length());

        @Getter
        class Pair<K, V> {
            private K first;
            private V second;

            public Pair(K first, V second) {
                this.first = first;
                this.second = second;
            }

        }

        HashSet<Integer> setX = new HashSet<>();
        HashSet<Integer> setY = new HashSet<>();
        Random randomX = new Random(113);
        Random randomY = new Random(226);
        Random randBit = new Random(113);

        List<Integer> arr = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        boolean[][] matrice = new boolean[coverImage.height()][coverImage.width()];

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                matrice[i][j] = false;

        int cnt = 0;
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
                System.out.println(cnt);
                System.out.println(x + ", " + y);
                throw new RuntimeException();
            }

            cnt ++;
//            setX.add(x);
//            setY.add(y);

            matrice[x][y] = true;

//            System.out.println("Pair :::: " + x + ", " + y);
            double[] values = coverImage.getMatrix().get(x, y);
//            System.out.println("Pixel BEFORE " + Arrays.toString(values));
//            int[] intValues = { (int) values[0], (int) values[1], (int) values[2] };

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                // 1101 0001 = 209
                // 001
                int randBitVal = ((int) values[0] & (1 << randomBitLoc)) >> randomBitLoc;
                int bit = secret.getCurrentBit();
                int xor = randBitVal ^ bit;
                values[0] = (double) (((int) values[0] & ~(1 << k)) | (xor << k));
//                intValues[0] = intValues[0] & ~((1 << k) | ((intValues[0] & (1 << randomBitLoc)) ^ secret.getCurrentBit()));
                arr.add(randomBitLoc);
                stringBuilder.append(bit);
            }

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int randBitVal = ((int) values[1] & (1 << randomBitLoc)) >> randomBitLoc;
                int bit = secret.getCurrentBit();
                int xor = randBitVal ^ bit;
                values[1] = (double) (((int) values[1] & ~(1 << k)) | (xor << k));
//                intValues[1] = intValues[1] & ~((1 << k) | ((intValues[1] & (1 << randomBitLoc)) ^ secret.getCurrentBit()));
                arr.add(randomBitLoc);

                stringBuilder.append(bit);
            }

            for(int k = 1; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int randBitVal = ((int) values[2] & (1 << randomBitLoc)) >> randomBitLoc;
                int bit = secret.getCurrentBit();
                int xor = randBitVal ^ bit;
                values[2] = (double) (((int) values[2] & ~(1 << k)) | (xor << k));
//                intValues[2] = intValues[2] & ~((1 << k) | ((intValues[2] & (1 << randomBitLoc)) ^ secret.getCurrentBit()));
                arr.add(randomBitLoc);
                stringBuilder.append(bit);
            }

//            values[0] = (double) intValues[0];
//            values[1] = (double) intValues[1];
//            values[2] = (double) intValues[2];

            coverImage.getMatrix().put(x, y, values);
//            System.out.println("Pixel " + Arrays.toString(values));

        }

//        System.out.println("-------------------------");
//        for(Integer pair : set) {
//            System.out.print("[" + pair.first + ", " + pair.second + "] ");
//        }
//        System.out.println("\n-------------------------");
//        System.out.println(arr);
//        System.out.println(stringBuilder);
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

        @Getter
        class Pair<K, V> {
            private K first;
            private V second;

            public Pair(K first, V second) {
                this.first = first;
                this.second = second;
            }

        }

        HashSet<Integer> setX = new HashSet<>();
        HashSet<Integer> setY = new HashSet<>();
        Random randomX = new Random(113);
        Random randomY = new Random(226);
        Random randBit = new Random(113);

        List<Integer> arr = new ArrayList<>();
        List<Integer> bits = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
//        int[] bits = new int[8];
        boolean[][] matrice = new boolean[coverImage.height()][coverImage.width()];

        for(int i = 0; i < coverImage.height(); i++)
            for(int j = 0; j < coverImage.width(); j++)
                matrice[i][j] = false;
        for(int i = 0; i < length; i++) {

            int t = 0;
            int x = randomX.nextInt(0, coverImage.height());
            int y;
            if(x == 0)
                y = randomY.nextInt(13, coverImage.width());
            else
                y = randomY.nextInt(0, coverImage.width());

//            while(set.contains(pair)) {
//                x = randomX.nextInt(0, coverImage.height());
//                if(x == 0)
//                    y = randomY.nextInt(13, coverImage.width());
//                else
//                    y = randomY.nextInt(0, coverImage.width());
//                pair.first = x;
//                pair.second = y;
//            }
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

//            setX.add(x);
//            setY.add(y);

            matrice[x][y] = true;

//            System.out.println("Pair :::: " +x+ ", " + y);
            double[] values = coverImage.getMatrix().get(x, y);
            int[] intValues = { (int) values[0], (int) values[1], (int) values[2] };

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int bit = (((intValues[0] & (1 << k)) >> k) ^ ((intValues[0] & (1 << randomBitLoc)) >> randomBitLoc));
                secret.createSecret(bit);
                arr.add(randomBitLoc);
//                bits[t] = bit;
                t++;
                bits.add(bit);
                stringBuilder.append(bit);
            }

            for(int k = 2; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int bit = (((intValues[1] & (1 << k)) >> k) ^ ((intValues[1] & (1 << randomBitLoc)) >> randomBitLoc));
                secret.createSecret(bit);
                arr.add(randomBitLoc);
//                bits[t] = bit;
                t++;
                bits.add(bit);
                stringBuilder.append(bit);
            }

            for(int k = 1; k > -1; k--) {
                int randomBitLoc = randBit.nextInt(3, 8);
                int bit = (((intValues[2] & (1 << k)) >> k) ^ ((intValues[2] & (1 << randomBitLoc)) >> randomBitLoc));
                secret.createSecret(bit);
                arr.add(randomBitLoc);
//                bits[t] = bit;
                t++;
                bits.add(bit);
                stringBuilder.append(bit);
            }
//            System.out.println("Pixel " + Arrays.toString(values));
//            for(int a = 0; a < 7; a++) secret.createSecret(bits[a]);

        }
//        System.out.println("-------------------------");
//        for(Pair<Integer, Integer> pair : set) {
//            System.out.print("[" + pair.first + ", " + pair.second + "] ");
//        }
//        System.out.println("\n-------------------------");
//        System.out.println(arr);
//        for (int i = bits.size() - 1; i > -1; i--) secret.createSecret(bits.get(i));

//        System.out.println(stringBuilder);
        secret.setSecret(secret.getSecret().length() + "." + secret.getSecret());
        return secret;
    }

    public static void main(String[] args) {

        OpenCV.loadLocally();

        List<String> tests = new ArrayList<>();

        tests.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec id metus imperdiet, rhoncus purus sed, vulputate diam. Integer sodales, quam sit amet sodales hendrerit, turpis erat auctor felis, non sagittis tellus sapien eu metus. Nulla pharetra accumsan viverra. Sed semper felis et nisi mattis commodo. Donec nulla lacus, varius nec quam ac, volutpat consequat magna. Praesent faucibus non enim id gravida. Praesent purus quam, commodo eget ipsum quis, rhoncus iaculis felis. Integer a quam in leo porttitor mattis et eget odio. Donec vehicula eros vel vehicula feugiat. Duis tincidunt, eros vitae ultrices finibus, augue augue volutpat turpis, fringilla imperdiet velit leo quis nulla. Nunc at justo efficitur, tristique arcu ut, pharetra diam. Fusce tempus tristique turpis, sed feugiat neque. Aenean nisl nunc, porta vitae enim vel, pretium imperdiet ex. Sed non elit maximus, malesuada orci in, luctus neque. Donec lacus ex, lobortis eget egestas et, placerat vitae metus. Fusce consequat nibh felis, nec facilisis lorem mollis sit amet.");
        tests.add("dadr");
        tests.add("");
        tests.add("passed failed but i dont know why");
        tests.add("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed fermentum blandit magna, vel accumsan odio elementum id. Nam condimentum dictum enim, nec tincidunt ante faucibus quis. Pellentesque velit magna, mattis ac pulvinar ac, feugiat ac odio. Quisque congue ante in sapien pulvinar fermentum. Curabitur ac arcu ut metus feugiat gravida sed quis nibh. Sed consectetur purus risus, ut porta dolor sodales vel. Integer eget ex sodales, facilisis velit non, varius ex. Duis eget efficitur leo. Nulla efficitur fringilla tortor, sit amet congue dolor sollicitudin id. Vivamus sapien nisi, hendrerit at pellentesque id, varius vitae urna. Donec in aliquam dolor, nec porta ante. In volutpat, libero et pharetra luctus, mi mauris placerat nunc, vitae laoreet mi lorem eu erat.\n" +
                "\n" +
                "\n" +
                "\n" +
                "Quisque nec commodo odio. Vivamus tincidunt accumsan tortor, nec blandit odio fringilla non. Vivamus ultricies ornare hendrerit. Praesent sit amet augue eget odio dictum pellentesque in non ipsum. Donec placerat nulla quam, vel interdum nunc euismod at. Mauris consectetur nisi at ultricies fermentum. Donec a nibh nunc. Cras blandit leo ut nibh accumsan facilisis. Fusce euismod interdum venenatis. Cras vulputate, turpis eu tincidunt fringilla, libero nulla viverra quam, eu vulputate quam dolor sit amet odio. Aliquam lobortis dolor in ipsum porttitor pretium.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed fermentum blandit magna, vel accumsan odio elementum id. Nam condimentum dictum enim, nec tincidunt ante faucibus quis. Pellentesque velit magna, mattis ac pulvinar ac, feugiat ac odio. Quisque congue ante in sapien pulvinar fermentum. Curabitur ac arcu ut metus feugiat gravida sed quis nibh. Sed consectetur purus risus, ut porta dolor sodales vel. Integer eget ex sodales, facilisis velit non, varius ex. Duis eget efficitur leo. Nulla efficitur fringilla tortor, sit amet congue dolor sollicitudin id. Vivamus sapien nisi, hendrerit at pellentesque id, varius vitae urna. Donec in aliquam dolor, nec porta ante. In volutpat, libero et pharetra luctus, mi mauris placerat nunc, vitae laoreet mi lorem eu erat.\n" +
                "\n" +
                "\n" +
                "\n" +
                "Quisque nec commodo odio. Vivamus tincidunt accumsan tortor, nec blandit odio fringilla non. Vivamus ultricies ornare hendrerit. Praesent sit amet augue eget odio dictum pellentesque in non ipsum. Donec placerat nulla quam, vel interdum nunc euismod at. Mauris consectetur nisi at ultricies fermentum. Donec a nibh nunc. Cras blandit leo ut nibh accumsan facilisis. Fusce euismod interdum venenatis. Cras vulputate, turpis eu tincidunt fringilla, libero nulla viverra quam, eu vulputate quam dolor sit amet odio. Aliquam lobortis dolor in ipsum porttitor pretium.");
        Secret secret = new Secret(tests.get(4));
        System.out.println("----------------");
        System.out.println(tests.get(4));
        System.out.println("----------------");
        CoverImage coverImage = new CoverImage();
        coverImage.readImage("/home/daniel/licenta/steg0vault/currentUserResources/car.png");

        Algorithm algorithm = new RandomPixelAndBitSelection();

        long startTime, stopTime;

        startTime = System.currentTimeMillis();
        algorithm.embed(coverImage, secret);
        stopTime = System.currentTimeMillis();

//        System.out.println("EMBEDDING DURATION " + String.valueOf((stopTime - startTime) / 1000));

        coverImage.save("/home/daniel/licenta/steg0vault/currentUserResources/RandomPixelSelection.png");

        CoverImage coverImage1 = new CoverImage();
        coverImage1.readImage("/home/daniel/licenta/steg0vault/currentUserResources/RandomPixelSelection.png");

        Algorithm algorithm1 = new RandomPixelAndBitSelection();

        startTime = System.currentTimeMillis();
        Secret secret1 = algorithm1.extract(coverImage1);
        stopTime = System.currentTimeMillis();

//        System.out.println("EXTRACTION DURATION " + String.valueOf((stopTime - startTime) / 1000));

        System.out.println(secret1.getSecret());

    }
}
