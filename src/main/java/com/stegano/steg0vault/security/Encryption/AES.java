package com.stegano.steg0vault.security.Encryption;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

@Slf4j
public class AES {
    private SecretKey key;
    private static final int KEY_SIZE = 128;
    private static final int T_LEN = 128;
    private byte[] IV;
    public void init() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
    }

    public void loadKeyAndIV() {
        // read from encryption.txt
        File myObj = new File("./currentUserResources" + "/encryption.txt");
        try {
            Scanner myReader = new Scanner(myObj);
            if (myReader.hasNextLine()) key = new SecretKeySpec(decode(myReader.nextLine()), "AES");
            if (myReader.hasNextLine()) IV = decode(myReader.nextLine());
            System.out.println(encode(key.getEncoded()) + " " + encode(IV));
            myReader.close();
        }
        catch(Exception e) {
            log.error("Cannot extract key and IV");
        }
    }

    public String createEncryptionKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEY_SIZE);
            key = generator.generateKey();
            Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            return encode(key.getEncoded()) + "\n" + encode(encryptionCipher.getIV());
        }
        catch (Exception e) {
            log.error("Cannot create encryption key");
            return "";
        }
    }

    public String encrypt(String message) {
        try {
            byte[] messageBytes = message.getBytes();
            Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] encryptedBytes = encryptionCipher.doFinal(messageBytes);
            System.out.println("Plain text: " + message + " Cipher text: " + encode(encryptedBytes));
            return encode(encryptedBytes);
        }
        catch (Exception e) {
            log.error("Cannot encrypt");
            return "";
        }
    }

    public String decrypt(String encryptedMessage)  {
        try {
            byte[] messageBytes = decode(encryptedMessage);
            Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
            decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] decryptedBytes = decryptionCipher.doFinal(messageBytes);
            return new String(decryptedBytes);
        }
        catch (Exception e) {
            log.error("Cannot decrypt");
            return "Cannot decrypt";
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
}
