package com.stegano.steg0vault.stego.toEmbed;

import com.stegano.steg0vault.security.Encryption.AES;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Secret {
    private String secret;
    private int index;
    private char chr;
    private int rollback;
    private AES aes = new AES();
    public Secret() {
        aes.loadKeyAndIV();
        secret = "";
        index = 0;
        rollback = index;
    }

    public void rollback() {
        this.rollback = this.index;
    }

    public void commitRollback() {
        this.index = this.rollback;
    }

    public Secret(String secret) {
        aes.loadKeyAndIV();
        String encryptedSecret = aes.encrypt(secret);
        System.out.println(encryptedSecret.length());
        this.secret = encryptedSecret.length() + "." + aes.encrypt(secret);
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

    public int getCurrentBitButDontMoveToNext() {
        if(hasToEmbed()) {
            int chr = secret.charAt(index / 8);
            for(int i = 0; i < index % 8; i++)
                chr /= 2;
            return chr % 2;
        }
        return 0;
    }

    public boolean hasToEmbed() {
        return index < secret.length() * 8;
    }

    public boolean canCreate() {
        try {
            int len = Integer.parseInt(secret.split("\\.")[0]);
            return (secret.length() - String.valueOf(len).length() - 1) >= len;
        } catch (NumberFormatException e) {
            return index / 8 > 2000;
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
            return "There is no embedded message!";
        }
    }

    public String getDecryptedMessage() {
        try {
            int len = Integer.parseInt(secret.split("\\.")[0]);
            return aes.decrypt(secret.substring(String.valueOf(len).length() + 1));
        } catch (NumberFormatException e) {
            return "There is no embedded message!";
        }
    }

}