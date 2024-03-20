package com.stegano.steg0vault.stego.image;

import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_UNCHANGED;

@Getter
public class CoverImage {

    private Mat matrix;
    private String fileName;

    public void readImage(String imagePath) {
        this.fileName = imagePath;
        this.matrix = Imgcodecs.imread(imagePath, IMREAD_UNCHANGED);
    }

    public int channels() {
        return matrix.channels();
    }
    public int capacity() { // NUMBER OF BYTES THAT CAN BE EMBEDDED IN THE COVER MEDIA
        return (channels() * this.matrix.width() * this.matrix.height()) / 8;
    }

    public int width() {
        return this.matrix.width();
    }
    public int height() {
        return this.matrix.height();
    }

    public int get(int channel, int oX, int oY) {
        return (int) this.matrix.get(oX, oY)[channel];
    }

    public void put(int channel, int oX, int oY, int value) {
        double[] values = this.matrix.get(oX, oY);
        values[channel] = (double) value;
        this.matrix.put(oX, oY, values);
    }

    public void save(String fileName) {
        Imgcodecs.imwrite(fileName, this.matrix);
    }
}