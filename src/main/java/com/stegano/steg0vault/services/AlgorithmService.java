package com.stegano.steg0vault.services;

import com.stegano.steg0vault.models.entities.AlgorithmEntity;
import com.stegano.steg0vault.repositories.AlgorithmRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlgorithmService {
    private final AlgorithmRepository algorithmRepository;

    public AlgorithmService(AlgorithmRepository algorithmRepository) {
        this.algorithmRepository = algorithmRepository;
    }

    public void addAlgorithm(AlgorithmEntity algorithm) {
        AlgorithmEntity algorithm1 = algorithmRepository.findAlgorithmEntityByName(algorithm.getName());
        if(algorithm1 == null)
            algorithmRepository.save(algorithm);
    }
}
