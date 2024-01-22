package com.stegano.steg0vault;

import com.stegano.steg0vault.stego.algorithms.Algorithm;
import com.stegano.steg0vault.stego.algorithms.LsbMatchingAlgorithm;
import com.stegano.steg0vault.stego.algorithms.LsbMatchingRevisitedAlgorithm;
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

		System.out.println("----------- main -----------");

		CoverImage pngImage = new CoverImage();
		pngImage.readImage("currentUserResources/Steg0Vault_Password_Test.png");
//		pngImage.readImage("currentUserResources/Hai.png");
		Secret secret = new Secret("danel e cel mai tare @@@@ haha");

		Algorithm algorithm = new LsbMatchingRevisitedAlgorithm();
		algorithm.embed(pngImage, secret);
		pngImage.save("currentUserResources/Hai_yes_transparent.png");

		CoverImage coverImage1 = new CoverImage();
		coverImage1.readImage("currentUserResources/Hai_yes_transparent.png");
		Algorithm algorithm1 = new LsbMatchingRevisitedAlgorithm();
		Secret secret1 = algorithm1.extract(coverImage1);

		// works with the following image formats: BMP, PNG, PPM, PNM, TIF, TIFF

		System.out.println("--------------------------" + secret1.getMessage() + "----------------------------------");

		System.out.println("-------- fin main ----------");
	}

}
