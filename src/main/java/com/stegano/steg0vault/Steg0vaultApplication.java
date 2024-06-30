package com.stegano.steg0vault;

import com.stegano.steg0vault.models.entities.AlgorithmEntity;
import com.stegano.steg0vault.services.AlgorithmService;
import com.stegano.steg0vault.services.UserService;
import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableJpaAuditing
public class Steg0vaultApplication {

	private static UserService userService;
	private static AlgorithmService algorithmService;

    public Steg0vaultApplication(UserService userService, AlgorithmService algorithmService) {
        Steg0vaultApplication.userService = userService;
        Steg0vaultApplication.algorithmService = algorithmService;
    }

    public static void main(String[] args) {
		SpringApplication.run(Steg0vaultApplication.class, args);
		OpenCV.loadLocally();
		userService.createAdmin();
		insertAlgorithms();
	}

	public static void insertAlgorithms() {
		AlgorithmEntity algorithm1 = AlgorithmEntity.builder()
				.name("LSB_REPLACEMENT")
				.build();
		AlgorithmEntity algorithm2 = AlgorithmEntity.builder()
				.name("LSB_MATCHING")
				.build();
		AlgorithmEntity algorithm3 = AlgorithmEntity.builder()
				.name("LSB_MATCHING_REVISITED")
				.build();
		AlgorithmEntity algorithm4 = AlgorithmEntity.builder()
				.name("BINARY_HAMMING_CODES")
				.build();
		AlgorithmEntity algorithm5 = AlgorithmEntity.builder()
				.name("RANDOM_PIXEL_SELECTION")
				.build();
		AlgorithmEntity algorithm6 = AlgorithmEntity.builder()
				.name("MULTI_BIT_PLANE")
				.build();
		algorithmService.addAlgorithm(algorithm1);
		algorithmService.addAlgorithm(algorithm2);
		algorithmService.addAlgorithm(algorithm3);
		algorithmService.addAlgorithm(algorithm4);
		algorithmService.addAlgorithm(algorithm5);
		algorithmService.addAlgorithm(algorithm6);
	}

}
