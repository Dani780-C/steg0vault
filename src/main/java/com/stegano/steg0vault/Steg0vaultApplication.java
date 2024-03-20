package com.stegano.steg0vault;

import com.stegano.steg0vault.stego.algorithms.Algorithm;
import com.stegano.steg0vault.stego.algorithms.LsbMatching;
import com.stegano.steg0vault.stego.algorithms.LsbMatchingRevisited;
import com.stegano.steg0vault.stego.algorithms.LsbReplacement;
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
	}

}
