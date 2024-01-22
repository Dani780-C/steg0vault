package com.stegano.steg0vault.stego.toEmbed;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Secret {
    private String message;
    private int charIndex;
    private int bitIndex;
    private byte[] currentCharBitStream = new byte[8];

    public Secret(String message) {
        this.message = message.length() + "." + message;
        this.resetBitStreamGenerator();
    }

    public Secret() {
        this.message = "";
        this.bitIndex = 0;
    }

    public char convertBitStreamToChar() {
        int ord = 0;
        for(int i = 0; i < 8; i++) {
            ord += (Math.pow(2, i) * currentCharBitStream[i]);
        }
        return (char) ord;
    }

    public void setBit(int bit) {
        if(this.canBuild()) {
            if(bitIndex < 8) {
                currentCharBitStream[bitIndex] = (byte) bit;
                bitIndex += 1;
            }
            else {
                this.message += this.convertBitStreamToChar();
                bitIndex = 0;
                this.setBit(bit);
            }
        }
    }

    public void resetBitStreamGenerator() {
        this.charIndex = 0;
        this.bitIndex = 0;
        for(byte i = 0; i < 8; i++) this.currentCharBitStream[i] = 0;
    }
    private void setCurrentCharBitStream(char chr) {
        int value = (byte) chr;
        int i = 0;
        while(value != 0) {
            this.currentCharBitStream[i] = (byte) (value % 2);
            value /= 2;
            i += 1;
        }
        while(i < 8) {
            this.currentCharBitStream[i] = 0;
            i++;
        }
    }

    public byte getCurrentBitOfBitStream() {
        if(charIndex < message.length()) {
            if(bitIndex == 0) {
                setCurrentCharBitStream(message.charAt(charIndex));
            }
            else if(bitIndex == 8) {
                bitIndex = 0;
                charIndex += 1;
                return getCurrentBitOfBitStream();
            }
            if(bitIndex < 8) {
                bitIndex += 1;
                return currentCharBitStream[bitIndex - 1];
            }
        }
        return 0;
    }

    public boolean canBuild() {
        if(this.message.length() == 0) {
            return true;
        }
        else if(this.message.length() > 10 && !this.message.contains(".")) {
            return false;
        }
        else if(this.message.contains(".")) {
            String number = (this.message.split("\\."))[0];
            for(char ch : number.toCharArray()) {
                if (!Character.isDigit(ch))
                    return false;
            }
            int msgLength = Integer.parseInt(number);
            return this.message.length() - number.length() - 1 != msgLength;
        }
        return true;
    }

    public boolean hasMessageToEmbed() {
        return charIndex < this.message.length();
    }
}
