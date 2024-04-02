package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import nu.pattern.OpenCV;

import java.util.Arrays;

class Pixel {
    int i, j, ch, value;
    Pixel(int startRow, int startCol, int startCh) {
        this.i = startRow;
        this.j = startCol;
        this.ch = startCh;
    }
    void set(int i, int j, int ch, int value) {
        this.i = i; this.j = j; this.ch = ch; this.value = value;
    }

    int[] getActualPosition() {
        return new int[]{i, j, ch};
    }

    void setActualPosition(int[] pos) {
        this.i = pos[0];
        this.j = pos[1];
        this.ch = pos[2];
    }

    int getValue(CoverImage coverImage) {
        return coverImage.get(ch, i, j);
    }

    void moveToNext(CoverImage coverImage) {
        if((i + 1) != coverImage.height() || (j + 1) != coverImage.width() || (ch + 1) != coverImage.channels()) {
            if(ch + 1 == coverImage.channels()) {
                ch = 0;
                j += 1;
            }
            else {
                ch += 1;
            }
            if (j == coverImage.width()) {
                j = 0;
                i += 1;
            }
        }
    }

    void switchLSBValue(int[] position, int nr, CoverImage coverImage) {

        Pixel newPix = new Pixel(position[0], position[1], position[2]);
        while(nr > 1) {
            newPix.moveToNext(coverImage);
            nr -= 1;
        }

        int[] pos = newPix.getActualPosition();
        int value = newPix.getValue(coverImage);
        int old_val = value;

        if(value % 2 == 0) value += 1;
        else value -= 1;

        System.out.println("MODIFICAM [I, J, CH]: " + Arrays.toString(pos) + " from " + old_val + " to new " + value);
        coverImage.put(pos[2], pos[0], pos[1], value);
    }

}

public class BinaryHammingCodes implements Algorithm {
    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        // header 12 bytes 12*8
        int p = 3;
        int pow_p = 16;

//        while(secret.getRealSecret().length()*8*(pow_p - 1) <= (coverImage.width()*coverImage.height()*coverImage.channels() - 12) * p) {
//            p += 1;
//            pow_p *= 2;
//        }

//        p = p - 1;
        String header = secret.getRealSecret().length() + "." + p + ".";
        Secret newSecret = new Secret(header);
        newSecret.setSecret(header);

        System.out.println("P = " + p);
//        System.out.println("BlockSize = " + ((pow_p / 2) - 1));
        System.out.println("Length bits = " + (secret.getRealSecret().length() * 8));
        System.out.println("Channels: " + coverImage.channels());
//        System.out.println("Header = " + header);

        // TODO: ##########################################################################3
        // EMBEDDING HEADER 12 BYTES
        boolean ok = true;
        for(int i = 0; i < coverImage.height() && ok; i++)
            for(int j = 0; j < coverImage.width() && ok; j++)
                for(int channel = 0; channel < coverImage.channels() && ok; channel++) {
                    if (!secret.hasToEmbed())
                        ok = false;
                    else
                        coverImage.put(channel, i, j, coverImage.get(channel, i, j) & ~1 | newSecret.getCurrentBit());
                }

        if(p == 0)
            throw new RuntimeException();


        secret.setSecret(secret.getRealSecret());

        int col = 1 + ((12 * 8) / coverImage.channels());
        int ch = (12 * 8) % coverImage.channels();

        Pixel pix = new Pixel(0, col, ch);

        int[] cM = new int[p];
        int bits_length = secret.getSecret().length() * 8;
        int block_size = (pow_p / 2) - 1;

        StringBuilder bits = new StringBuilder();

//        if(bits_length < p) p = bits_length;

        // TODO: #################################################################################
        int block_index = 0;
        while (bits_length > 0) {

            if (bits_length < p) {
                p = bits_length;
                int po = 0;
                block_size = 1;
                while(po < p) {
                    block_size = block_size * 2;
                    po += 1;
                }
                block_size -= 1;
            }

            System.out.println("\n----------------------------------------");
            System.out.println("BLOCK " + block_index);
            System.out.println("block_size: " + block_size);

            int k = 0;

            int[] actPos = pix.getActualPosition();
            System.out.println("start block index [i, j, ch]: " + Arrays.toString(actPos));

            while(k < p) {

                int sum = 0;

                String s1 = "";
                String s2 = "";
                for (int i = 1; i <= block_size; i++) {
                    int bit = i & (1 << k);
                    s1 += bit + " ";
                    int pixelLSBValue = pix.getValue(coverImage) % 2;
                    s2 += pixelLSBValue + " ";
                    if((bit & (pixelLSBValue << k)) != 0) sum += 1;
                    pix.moveToNext(coverImage);
                }
//                System.out.println("#######");
//                System.out.println(s1);
//                System.out.println(s2);
//                System.out.println("----------------- plus");


                cM[k] = sum % 2;
                k += 1;
                if( k != p )
                    pix.setActualPosition(actPos);
            }

            System.out.println("###########################");
            for(int i = 0; i < p; i++) System.out.print(cM[i] + " ");

            String s3 = "";
            for(int i = 0; i < p; i++) {
                int b = secret.getCurrentBit();
                s3 += b + " ";
                cM[i] = Math.abs(cM[i] - b);
                bits.append(b);
            }
            System.out.println();
            System.out.println(s3);
            System.out.println("------------- minus");
            for(int i = 0; i < p; i++) System.out.print(cM[i] + " ");

            int nr = 0;
            for(int i = 0; i < p; i++) {
                nr += (cM[p - i - 1] << (i));
            }

            System.out.println("NUMAR: " + nr);
            if(nr != 0) {
                pix.switchLSBValue(actPos, nr, coverImage);
            }

            bits_length -= p;
            block_index += 1;
        }
        System.out.println("Bits [" + bits.length() + "]: " + bits);
    }

    @Override
    public Secret extract(CoverImage coverImage) {
        Secret secret = new Secret();
        boolean ok = true;
        for (int i = 0; i < coverImage.height() && ok; i++)
            for (int j = 0; j < coverImage.width() && ok; j++)
                for (int channel = 0; channel < coverImage.channels() && ok; channel++) {
                    String s = secret.getSecret();
                    if (s.matches("[0-9]+\\.[0-9]+\\.")) ok = false;
                    else secret.createSecret(coverImage.get(channel, i, j) % 2);
                }

        int length = Integer.parseInt(secret.getSecret().split("\\.")[0]);
//        int p = Integer.parseInt(secret.getSecret().split("\\.")[1]);

        int p = 3;
        int bits_length = length * 8;
        int pow_p = 1; int ij = 0;
        while(ij < p) { pow_p = pow_p * 2; ij++;}
        int block_size = pow_p - 1;

        int col = 1 + ((12 * 8) / coverImage.channels());
        int ch = (12 * 8) % coverImage.channels();

        Pixel pix = new Pixel(0, col, ch);

        Secret newSecret = new Secret();
        StringBuilder bits = new StringBuilder();
        int block_index = 0;
        while (bits_length > 0) {

            if (bits_length < p) {
                p = bits_length;
                int po = 0;
                block_size = 1;
                while(po < p) {
                    block_size = block_size * 2;
                    po += 1;
                }
                block_size -= 1;
            }

            System.out.println("\n----------------------------------------");
            System.out.println("BLOCK " + block_index);
            System.out.println("block_size: " + block_size);


            String s1 = "";
            int k = 0;

            int[] actPos = pix.getActualPosition();
            System.out.println("start block index [i, j, ch]: " + Arrays.toString(actPos));


            while(k < p) {

                int sum = 0;
                for (int i = 1; i <= block_size; i++) {
                    int bit = i & (1 << k);
                    int pixelLSBValue = pix.getValue(coverImage) % 2;

                    if((bit & (pixelLSBValue << k)) != 0) sum += 1;
                    pix.moveToNext(coverImage);
                }

//                newSecret.createSecret(sum % 2);
//                bits.append(sum % 2);

                s1 += sum % 2;

                k += 1;
                if(k != p)
                    pix.setActualPosition(actPos);
            }

            System.out.println("BITS: " +  s1);

            for(int i = s1.length() - 1; i > -1; i--) {
//                newSecret.createSecret(s1.charAt(i));
                bits.append(s1.charAt(i));
            }

            bits_length -= p;
            block_index += 1;
        }
        // 0010011010000110
        // 0010011010000110
        for(int i = 0; i < bits.length(); i++)
            if(bits.charAt(i) == '0')
                newSecret.createSecret(0);
            else
                newSecret.createSecret(1);
        System.out.println("Bits [" + bits.length() + "]: " + bits);
        return newSecret;
    }

    public static void main(String[] args) {


        OpenCV.loadLocally();
        Secret secret = new Secret("test 123");
        CoverImage coverImage = new CoverImage();
        coverImage.readImage("/home/daniel/licenta/steg0vault/currentUserResources/tree.png");

        Algorithm algorithm = new BinaryHammingCodes();
        algorithm.embed(coverImage, secret);

        coverImage.save("/home/daniel/licenta/steg0vault/currentUserResources/HammingCodes.png");

        CoverImage coverImage1 = new CoverImage();
        coverImage1.readImage("/home/daniel/licenta/steg0vault/currentUserResources/HammingCodes.png");

        Algorithm algorithm1 = new BinaryHammingCodes();
        Secret secret1 = algorithm1.extract(coverImage1);

        System.out.println(secret1.getSecret());

    }

}
