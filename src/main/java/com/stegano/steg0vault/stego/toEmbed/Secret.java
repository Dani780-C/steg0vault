package com.stegano.steg0vault.stego.toEmbed;

import lombok.Getter;

@Getter
public class Secret {
    private String secret;
    private int index;
    private char chr;

    public Secret() {
        secret = "";
        index = 0;
    }
    public Secret(String secret) {
        this.secret = secret.length() + "." + secret;
        this.index = 0;
    }
    public int length() { // The secret format to embed: "header.message" [header is the length of the message]
        return secret.length();
    }

    public int getCurrentBit() {
        if(hasToEmbed()) {
            int chr = secret.charAt(index / 8);
            for(int i = 0; i < index % 8; i++)
                chr /= 2;

            index += 1;
            return chr % 2;
        }
        return 0;
    }

    public boolean hasToEmbed() {
        return index < secret.length() * 8;
    }

    public boolean canExtract() {
        try {
            int len = Integer.parseInt(secret.split("\\.")[0]);
            return (secret.length() - String.valueOf(len).length() - 1) < len;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public void createSecret(int bit) {
        if(index % 8 == 0) {
            this.chr = (char) bit;
        }
        else {
            this.chr = (char) ((bit << (index % 8)) + ((int) this.chr));
            if(index % 8 == 7) secret += this.chr;
        }
        index += 1;
    }

    public String getRealSecret() {
        try {
            int len = Integer.parseInt(secret.split("\\.")[0]);
            return secret.substring(String.valueOf(len).length() + 1);
        } catch (NumberFormatException e) {
            return "";
        }
    }
}