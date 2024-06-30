package com.stegano.steg0vault.stego.algorithms;

import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import lombok.extern.slf4j.Slf4j;

class Pixel {
    int i, j, ch;

    Pixel(int startRow, int startCol, int startCh) {
        this.i = startRow;
        this.j = startCol;
        this.ch = startCh;
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

        if(value % 2 == 0) value += 1;
        else value -= 1;

        coverImage.put(pos[2], pos[0], pos[1], value);
    }

}


@Slf4j
public class BinaryHammingCodes implements Algorithm {

    @Override
    public void embed(CoverImage coverImage, Secret secret) {
        int p = 1;
        int pow_p = 2;

        while(secret.getRealSecret().length()*8*(pow_p - 1) <= (coverImage.width()*coverImage.height()*coverImage.channels() - 12) * p) {
            p += 1;
            pow_p *= 2;
        }

        p = p - 1;

        String header = secret.getRealSecret().length() + "." + p + ".";
        Secret newSecret = new Secret();
        newSecret.setSecret(header);

        boolean ok = true; int cnt = 0;
        for(int i = 0; i < coverImage.height() && ok; i++)
            for(int j = 0; j < coverImage.width() && ok; j++)
                for(int channel = 0; channel < coverImage.channels() && ok; channel++) {
                    coverImage.put(channel, i, j, coverImage.get(channel, i, j) & ~1 | newSecret.getCurrentBit());
                    cnt += 1;
                    if (cnt > newSecret.getSecret().length() * 8)
                        ok = false;
                }

        if(p == 0)
            throw new RuntimeException();

        secret.setSecret(secret.getRealSecret());

        int col = 1 + ((12 * 8) / coverImage.channels());
        int ch = (12 * 8) % coverImage.channels();

        Pixel pix = new Pixel(1, col, ch);

        int[] cM = new int[p];
        int bits_length = secret.getSecret().length() * 8;
        int block_size = (pow_p / 2) - 1;

        if(bits_length < p) p = bits_length;

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

            int k = 0;

            int[] actPos = pix.getActualPosition();

            while(k < p) {

                int sum = 0;

                for (int i = 1; i <= block_size; i++) {
                    int bit = i & (1 << k);
                    int pixelLSBValue = pix.getValue(coverImage) % 2;

                    if((bit & (pixelLSBValue << k)) != 0) sum += 1;

                    pix.moveToNext(coverImage);
                }

                cM[k] = sum % 2;
                k += 1;
                if( k != p )
                    pix.setActualPosition(actPos);
            }

            for(int i = 0; i < p; i++) {
                int b = secret.getCurrentBit();
                cM[i] = Math.abs(cM[i] - b);
            }

            int nr = 0;
            for(int i = 0; i < p; i++) {
                nr += (cM[i] << (i));
            }

            if(nr != 0) {
                pix.switchLSBValue(actPos, nr, coverImage);
            }

            bits_length -= p;
        }
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
        int p = Integer.parseInt(secret.getSecret().split("\\.")[1]);

        int bits_length = length * 8;
        int pow_p = 1; int ij = 0;
        while(ij < p) { pow_p = pow_p * 2; ij++;}
        int block_size = pow_p - 1;

        int col = 1 + ((12 * 8) / coverImage.channels());
        int ch = (12 * 8) % coverImage.channels();

        Pixel pix = new Pixel(1, col, ch);

        Secret newSecret = new Secret();
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

            int k = 0;

            int[] actPos = pix.getActualPosition();

            while(k < p) {

                int sum = 0;
                for (int i = 1; i <= block_size; i++) {
                    int bit = i & (1 << k);
                    int pixelLSBValue = pix.getValue(coverImage) % 2;
                    if((bit & (pixelLSBValue << k)) != 0) sum += 1;
                    pix.moveToNext(coverImage);
                }

                newSecret.createSecret(sum % 2);

                k += 1;
                if(k != p)
                    pix.setActualPosition(actPos);
            }

            bits_length -= p;
        }

        newSecret.setSecret(newSecret.getSecret().length() + "." + newSecret.getSecret());
        return newSecret;
    }

}
