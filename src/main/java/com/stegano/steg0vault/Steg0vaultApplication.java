package com.stegano.steg0vault;

import com.stegano.steg0vault.stego.algorithms.Algorithm;
import com.stegano.steg0vault.stego.algorithms.LsbReplacementAlgorithm;
import com.stegano.steg0vault.stego.image.CoverImage;
import com.stegano.steg0vault.stego.toEmbed.Secret;
import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Steg0vaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(Steg0vaultApplication.class, args);
		OpenCV.loadLocally();


		Secret secret = new Secret("This is a test message for LSB replacement!");
		CoverImage coverImage = new CoverImage();
		coverImage.readImage("currentUserResources/logo_1_transparent_background.png");

		Algorithm lsbReplacementAlgorithm = new LsbReplacementAlgorithm();
		lsbReplacementAlgorithm.embed(coverImage, secret);

		CoverImage coverImage1 = new CoverImage();
		coverImage1.readImage("currentUserResources/STEG00_LSB.png");

		Algorithm lsbReplacementAlgorithm1 = new LsbReplacementAlgorithm();
		Secret secret1 = lsbReplacementAlgorithm1.extract(coverImage1);
		System.out.println(secret1.getRealSecret());
	}

}
