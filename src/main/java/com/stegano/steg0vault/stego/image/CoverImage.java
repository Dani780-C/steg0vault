package com.stegano.steg0vault.stego.image;

import lombok.Getter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_UNCHANGED;

@Getter
public class CoverImage {

    private int width;
    private int height;
    private Mat matrix;
    private int axisX;
    private int axisY;
    private byte indexRGB;
    private int channels;
    public void readImage(String imagePath) {
        this.matrix = Imgcodecs.imread(imagePath, IMREAD_UNCHANGED);
        this.height = this.matrix.height();
        this.width = this.matrix.width();
        this.axisX = 0;
        this.axisY = 0;
        this.indexRGB = 0;
        this.channels = matrix.channels();
        System.out.println(this.channels);
//        List<Mat> channels = new ArrayList<>(4);
//        Core.split(matrix, channels);
//        Mat alpha = channels.get(3);
//        Core.bitwise_not(alpha,alpha);
    }

    public int getBit() {
        if(axisY < height) {
            if(axisX < width) {
                if(indexRGB < 3) {
                    indexRGB += 1;
                    return ((int)matrix.get(axisY, axisX)[indexRGB - 1]) % 2;
                }
                else if(indexRGB == 3) {
                    indexRGB = 0;
                    axisX += 1;
                    return getBit();
                }
            }
            else {
                axisX = 0;
                axisY += 1;
                return getBit();
            }
        }
        return 0;
    }

    public int[] getGroup() {
        if(axisY < height) {
            if(axisX < width) {
                int[] arr = {axisY, axisX, indexRGB, (int) matrix.get(axisY, axisX)[indexRGB]};
                indexRGB++;
                if(indexRGB == matrix.channels()) {
                    indexRGB = 0;
                    axisX++;
                }
//                indexRGB = (byte) ((indexRGB) % matrix.channels());
                System.out.println("=================== GET =============== " + Arrays.toString(arr));
                return arr;
            }
            axisX = 0;
            axisY++;
            return getGroup();
        }
        return null;
    }

    public int getCurrentBit() {
        if(axisY < height) {
            if(axisX < width) {
                if(indexRGB < 3) {
                    if(indexRGB == 0) {
                        return ((int) matrix.get(axisY, axisX)[0]);
                    }
                    else if(indexRGB == 1) {
                        return ((int) matrix.get(axisY, axisX)[1]);
                    }
                    else {
                        return ((int) matrix.get(axisY, axisX)[2]);
                    }
                }
            }
        }
        return 0;
    }

    public void setCurrentBit(byte bit) {
        if(axisY < height) {
            if(axisX < width) {
                if(indexRGB < 3) {
                    if(indexRGB == 0) {
                        matrix.put(
                                axisY, axisX,
                                (((int) matrix.get(axisY, axisX)[0]) & ~1 | bit),
                                (int) matrix.get(axisY, axisX)[1],
                                (int) matrix.get(axisY, axisX)[2]
                        );
                    }
                    else if(indexRGB == 1) {
                        matrix.put(
                                axisY, axisX,
                                (int) matrix.get(axisY, axisX)[0],
                                (((int) matrix.get(axisY, axisX)[1]) & ~1 | bit),
                                (int) matrix.get(axisY, axisX)[2]
                        );
                    }
                    else {
                        matrix.put(
                                axisY, axisX,
                                (int) matrix.get(axisY, axisX)[0],
                                (int) matrix.get(axisY, axisX)[1],
                                (((int) matrix.get(axisY, axisX)[2]) & ~1 | bit)
                        );
                    }
                    indexRGB += 1;
                }
                else{
                    indexRGB = 0;
                    axisX += 1;
                    setCurrentBit(bit);
                }
            }
            else {
                indexRGB = 0;
                axisX = 0;
                axisY += 1;
                setCurrentBit(bit);
            }
        }
    }

    public boolean hasCapacity() {
        return axisX != width && axisY != height;
    }

    public void save(String imagePath) {
        Imgcodecs.imwrite(imagePath, this.matrix);
    }

}

